package com.sarthi.SRailPad.service.scrap;

import com.sarthi.SRailPad.dto.scrap.RailScrapVerificationRequest;
import com.sarthi.SRailPad.entity.raipadMapping.RailPoiIeMapping;
import com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants;
import com.sarthi.SRailPad.entity.scrap.RailScrapVerification;
import com.sarthi.SRailPad.entity.scrap.RailScrapVerificationAudit;
import com.sarthi.SRailPad.entity.scrap.RailScrapVerificationImage;
import com.sarthi.SRailPad.repository.RailPoiIeMappingRepository;
import com.sarthi.SRailPad.repository.RailVendorPlantsRepository;
import com.sarthi.SRailPad.repository.scrap.RailScrapVerificationAuditRepository;
import com.sarthi.SRailPad.repository.scrap.RailScrapVerificationImageRepository;
import com.sarthi.SRailPad.repository.scrap.RailScrapVerificationRepository;
import com.sarthi.entity.UserMaster;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.exception.InvalidInputException;
import com.sarthi.service.AzureBlobStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Rail Pad Rejection Basket, Scrap Verification and Scrap Ledger (SRS - Rejection basket).
 *
 * Rejections are read live from the inspection tables (process verification rejections and final lots
 * marked REJECTED); only scrap verifications are stored. Pending scrap = rejected - scrapped, kept per
 * plant + product type.
 */
@Service
public class RailRejectionBasketService {

    private static final Logger log = LoggerFactory.getLogger(RailRejectionBasketService.class);

    public static final String DECLARATION_TEXT =
            "I certify that the rejected rail pads have been cut into pieces and made unusable as per IRS T-55 specifications.";

    private static final int EDIT_WINDOW_MINUTES = 60;
    private static final int PRODUCTION_DAYS = 30;
    private static final String IMAGE_CONTAINER = "scrap-images";
    private static final int TARGET_IMAGE_KB = 1400;
    private static final int MAX_PHOTOS_PER_TYPE = 5;
    private static final String IE_TYPE_PROCESS = "PROCESS_IE";

    @Autowired private RailScrapVerificationRepository scrapRepo;
    @Autowired private RailScrapVerificationImageRepository imageRepo;
    @Autowired private RailScrapVerificationAuditRepository auditRepo;
    @Autowired private RailVendorPlantsRepository vendorPlantsRepo;
    @Autowired private RailPoiIeMappingRepository poiIeMappingRepo;
    @Autowired private AzureBlobStorageService azureBlobStorageService;

    // =====================================================================
    // Access scope
    // =====================================================================

    /** Which plants the logged-in user may see, and where they may record scrap verifications. */
    private static final class Scope {
        boolean allPlants;
        final Set<String> plantIds = new HashSet<>();
        final Set<String> processPlantIds = new HashSet<>();
        Long userId;
        String userName;

        boolean canSee(String plantId) { return allPlants || plantIds.contains(plantId); }
        boolean canScrap(String plantId) { return canSee(plantId); }
    }

    private Scope resolveScope(Object principal) {
        Scope scope = new Scope();
        if (!(principal instanceof UserMaster user)) {
            scope.allPlants = true; // Railway Board / other signed-in roles: read-only, all plants
            return scope;
        }
        scope.userId = user.getUserId() != null ? user.getUserId().longValue() : null;
        scope.userName = user.getFullName() != null && !user.getFullName().isBlank() ? user.getFullName() : user.getUsername();
        String role = user.getRoleName() != null ? user.getRoleName().toUpperCase() : "";

        List<RailPoiIeMapping> mappings = user.getUserId() != null
                ? poiIeMappingRepo.findByIeUserId(user.getUserId()) : List.of();
        // "IE" as a whole word (Rail Process IE, Rail Main IE, Railpad IE), so roles like "CLIENT" don't match
        boolean ieRole = java.util.regex.Pattern.compile("\\bIE\\b").matcher(role).find();
        if (!mappings.isEmpty() || ieRole) {
            for (RailPoiIeMapping m : mappings) {
                String pid = normPlant(m.getPlantId());
                if (pid == null) continue;
                scope.plantIds.add(pid);
                scope.processPlantIds.add(pid);
            }
            return scope;
        }
        if (role.contains("VENDOR")) {
            String ownCode = normPlant(user.getEmployeeCode());
            for (RailVendorPlants p : vendorPlantsRepo.findAll()) {
                boolean own = (ownCode != null && ownCode.equals(normPlant(p.getVendorCode())))
                        || (scope.userId != null && scope.userId.equals(p.getVendorId()));
                if (own && normPlant(p.getPlantId()) != null) scope.plantIds.add(normPlant(p.getPlantId()));
            }
            return scope;
        }
        scope.allPlants = true; // RITES Admin, CM, Railway Board: read-only
        return scope;
    }

    private void assertCanSee(Scope scope, String plantId) {
        if (!scope.canSee(plantId)) {
            throw error("You do not have access to this plant's rejection basket.");
        }
    }

    // =====================================================================
    // Rejection Basket (summary)
    // =====================================================================

    public List<Map<String, Object>> getBasket(Object principal, String vendorCode, String plantId, String productType) {
        Scope scope = resolveScope(principal);
        LocalDate since = LocalDate.now().minusDays(PRODUCTION_DAYS - 1L);
        Map<String, RailVendorPlants> plants = plantMap();
        Map<String, BasketRow> rows = new LinkedHashMap<>();

        for (Object[] r : scrapRepo.sumProcessRejectedGrouped(since)) {
            BasketRow row = row(rows, str(r[0]), str(r[1]), str(r[2]));
            if (row == null) continue;
            row.processRejected += num(r[3]);
            row.rejected30 += num(r[4]);
        }
        for (Object[] r : scrapRepo.sumFinalRejectedGrouped(since)) {
            BasketRow row = row(rows, str(r[0]), str(r[1]), str(r[2]));
            if (row == null) continue;
            row.finalRejected += num(r[3]);
            row.rejected30 += num(r[4]);
        }
        for (Object[] r : scrapRepo.sumProductionGrouped(since)) {
            BasketRow row = row(rows, str(r[0]), str(r[1]), str(r[2]));
            if (row != null) row.production30 += num(r[3]);
        }
        for (Object[] r : scrapRepo.sumScrappedGrouped()) {
            BasketRow row = row(rows, str(r[0]), str(r[1]), str(r[2]));
            if (row == null) continue;
            row.scrapped += num(r[3]);
            row.lastScrapDate = toDate(r[4]);
        }

        String vendorFilter = normPlant(vendorCode);
        String plantFilter = normPlant(plantId);
        String typeFilter = productKey(productType);
        List<Map<String, Object>> out = new ArrayList<>();
        for (BasketRow row : rows.values()) {
            if (!scope.canSee(row.plantId)) continue;
            // Only plants with something in the basket (rejections or scraps)
            if (row.processRejected + row.finalRejected + row.scrapped == 0) continue;
            RailVendorPlants plant = plants.get(row.plantId);
            String rowVendor = plant != null ? normPlant(plant.getVendorCode()) : null;
            if (vendorFilter != null && !vendorFilter.equals(rowVendor)) continue;
            if (plantFilter != null && !plantFilter.equals(row.plantId)) continue;
            if (typeFilter != null && !typeFilter.equals(row.productKey)) continue;

            long totalRejected = row.processRejected + row.finalRejected;
            long pendingRaw = totalRejected - row.scrapped;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("plantId", row.plantId);
            m.put("vendorCode", plant != null ? plant.getVendorCode() : null);
            m.put("manufacturer", plant != null ? plant.getCompanyName() : null);
            m.put("placeOfInspection", plant != null ? plant.getPlantName() : row.plantId);
            m.put("productType", row.productType);
            m.put("productionLast30Days", row.production30);
            m.put("rejectedLast30Days", row.rejected30);
            m.put("processRejected", row.processRejected);
            m.put("finalRejected", row.finalRejected);
            m.put("totalRejected", totalRejected);
            m.put("totalScrapped", row.scrapped);
            m.put("pendingScrapQty", Math.max(0, pendingRaw));
            // Rejections were reduced after scrapping: more scrapped than currently rejected
            m.put("balanceMismatch", pendingRaw < 0);
            m.put("lastScrapDate", row.lastScrapDate != null ? row.lastScrapDate.toString() : null);
            m.put("canScrap", scope.canScrap(row.plantId));
            out.add(m);
        }
        out.sort(Comparator.comparing((Map<String, Object> m) -> String.valueOf(m.get("manufacturer")))
                .thenComparing(m -> String.valueOf(m.get("placeOfInspection")))
                .thenComparing(m -> String.valueOf(m.get("productType"))));
        return out;
    }

    private static final class BasketRow {
        String plantId, productKey, productType;
        long processRejected, finalRejected, rejected30, production30, scrapped;
        LocalDate lastScrapDate;
    }

    private BasketRow row(Map<String, BasketRow> rows, String plantId, String key, String display) {
        String pid = normPlant(plantId);
        if (pid == null || key == null || key.isBlank()) return null;
        return rows.computeIfAbsent(pid + "|" + key, k -> {
            BasketRow r = new BasketRow();
            r.plantId = pid;
            r.productKey = key;
            r.productType = display != null ? display.trim() : key;
            return r;
        });
    }

    // =====================================================================
    // Rejection history (detail view)
    // =====================================================================

    public List<Map<String, Object>> getRejectionHistory(Object principal, String plantId, String productType,
                                                         LocalDate from, LocalDate to, String stage) {
        Scope scope = resolveScope(principal);
        String pid = requirePlant(plantId);
        String key = requireProduct(productType);
        assertCanSee(scope, pid);

        List<Map<String, Object>> rows = new ArrayList<>();
        for (RejectionEntry e : loadRejections(pid, key)) {
            if (stage != null && !stage.isBlank() && !stage.equalsIgnoreCase(e.stage)) continue;
            if (from != null && (e.date == null || e.date.isBefore(from))) continue;
            if (to != null && (e.date == null || e.date.isAfter(to))) continue;
            rows.add(e.toMap());
        }
        Collections.reverse(rows); // newest first
        return rows;
    }

    /** One rejection, from either the process verification or a rejected final lot. */
    private static final class RejectionEntry {
        LocalDate date;
        LocalDateTime createdAt;
        String stage;          // PROCESS / FINAL
        String reference;      // Batch 101 / Lot 5
        String callOrShift;    // inspection call no or shift id
        String productType;
        String defectReason;
        long qty;
        String addedBy;
        Long sourceId;         // verification id (process) or lot result id (final)

        Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("date", date != null ? date.toString() : null);
            m.put("stage", stage);
            m.put("reference", reference);
            m.put("callOrShift", callOrShift);
            m.put("productType", productType);
            m.put("defectReason", defectReason);
            m.put("rejectedQty", qty);
            m.put("addedBy", addedBy);
            m.put("sourceId", sourceId);
            return m;
        }
    }

    private List<RejectionEntry> loadRejections(String plantId, String productKey) {
        List<RejectionEntry> list = new ArrayList<>();
        for (Object[] r : scrapRepo.findProcessRejections(plantId, productKey)) {
            RejectionEntry e = new RejectionEntry();
            e.date = toDate(r[0]);
            String shift = str(r[1]);
            e.sourceId = r[2] != null ? ((Number) r[2]).longValue() : null;
            Object requestId = r[3];
            e.stage = "PROCESS";
            e.productType = str(r[4]);
            e.reference = "Batch " + Objects.toString(str(r[5]), "-");
            e.defectReason = str(r[6]);
            e.qty = num(r[7]);
            e.addedBy = nameOr(str(r[9]), r[8]);
            e.createdAt = toDateTime(r[10]);
            e.callOrShift = (shift != null ? "Shift " + shift : "")
                    + (requestId != null ? (shift != null ? " / " : "") + "Declaration #" + requestId : "");
            list.add(e);
        }
        for (Object[] r : scrapRepo.findFinalRejections(plantId, productKey)) {
            RejectionEntry e = new RejectionEntry();
            e.date = toDate(r[0]);
            e.sourceId = r[2] != null ? ((Number) r[2]).longValue() : null;
            e.stage = "FINAL";
            e.callOrShift = str(r[3]);
            e.reference = "Lot " + Objects.toString(str(r[4]), "-");
            e.productType = str(r[5]);
            e.qty = num(r[6]);
            e.addedBy = nameOr(str(r[8]), r[7]);
            e.createdAt = toDateTime(r[9]);
            List<String> reasons = new ArrayList<>();
            if (str(r[10]) != null) reasons.add("Failed: " + str(r[10]));
            if (str(r[11]) != null) reasons.add("Weight test rejected");
            if (reasons.isEmpty()) reasons.add(str(r[12]) != null ? str(r[12]) : "Lot permanently rejected");
            e.defectReason = String.join("; ", reasons);
            list.add(e);
        }
        list.sort(Comparator.comparing((RejectionEntry e) -> e.date, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(e -> e.createdAt, Comparator.nullsLast(Comparator.naturalOrder())));
        return list;
    }

    // =====================================================================
    // Scrap ledger (passbook)
    // =====================================================================

    public Map<String, Object> getLedger(Object principal, String plantId, String productType,
                                         LocalDate from, LocalDate to, String txnType) {
        Scope scope = resolveScope(principal);
        String pid = requirePlant(plantId);
        String key = requireProduct(productType);
        assertCanSee(scope, pid);

        // Build every transaction in date order, then compute the running balance over all of them
        RailVendorPlants plant = plantMap().get(pid);
        List<Map<String, Object>> all = new ArrayList<>();
        for (RejectionEntry e : loadRejections(pid, key)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("rowType", "REJECTION");
            m.put("date", e.date != null ? e.date.toString() : null);
            m.put("entryDate", e.date != null ? e.date.toString() : null);
            m.put("sortTime", e.createdAt);
            m.put("transactionType", "PROCESS".equals(e.stage) ? "Rejection (Process)" : "Rejection (Final)");
            m.put("stage", e.stage);
            m.put("stageOfInspection", e.stage);
            m.put("reference", e.reference);
            m.put("scrapVerificationId", "—");
            m.put("callOrShift", e.callOrShift);
            m.put("shift", e.callOrShift != null && e.callOrShift.startsWith("Shift ") ? e.callOrShift.replace("Shift ", "") : "—");
            m.put("unitLineShedNo", plant != null ? plant.getPlantName() : pid);
            m.put("defectReason", e.defectReason);
            m.put("remarks", e.defectReason);
            m.put("rejectedQty", e.qty);
            m.put("scrappedQty", null);
            m.put("engineer", e.addedBy);
            m.put("ieName", e.addedBy);
            m.put("sourceId", e.sourceId);
            m.put("imageUrls", Collections.emptyList());
            all.add(m);
        }
        List<RailScrapVerification> scraps = scrapRepo.findActive(pid, key);
        List<Long> scrapIds = scraps.stream().map(RailScrapVerification::getId).toList();
        Map<Long, List<String>> imagesByScrapId = new HashMap<>();
        if (!scrapIds.isEmpty()) {
            for (RailScrapVerificationImage img : imageRepo.findByScrapVerificationIdIn(scrapIds)) {
                imagesByScrapId.computeIfAbsent(img.getScrapVerificationId(), k -> new ArrayList<>()).add(img.getImageUrl());
            }
        }
        for (RailScrapVerification s : scraps) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("rowType", "SCRAP");
            m.put("date", s.getScrappingDate() != null ? s.getScrappingDate().toString() : null);
            m.put("entryDate", s.getScrappingDate() != null ? s.getScrappingDate().toString() : null);
            m.put("sortTime", s.getCreatedDate());
            m.put("transactionType", "Scrap Verification");
            m.put("stage", null);
            m.put("stageOfInspection", "—");
            m.put("reference", s.getVerificationNo());
            m.put("scrapVerificationId", s.getVerificationNo());
            m.put("shift", s.getShift() != null ? s.getShift() : "—");
            m.put("callOrShift", s.getShift() != null ? "Shift " + s.getShift() : null);
            m.put("unitLineShedNo", plant != null ? plant.getPlantName() : pid);
            m.put("defectReason", s.getRemarks());
            m.put("remarks", s.getRemarks());
            m.put("rejectedQty", null);
            m.put("scrappedQty", (long) s.getQtyScrapped());
            m.put("engineer", s.getIeName());
            m.put("ieName", s.getIeName());
            m.put("verificationId", s.getId());
            m.put("editable", isEditable(s, scope));
            m.put("imageUrls", imagesByScrapId.getOrDefault(s.getId(), Collections.emptyList()));
            all.add(m);
        }
        all.sort(Comparator.comparing((Map<String, Object> m) -> toDate(m.get("date")), Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(m -> (LocalDateTime) m.get("sortTime"), Comparator.nullsLast(Comparator.naturalOrder())));

        long balance = 0, totalRejected = 0, totalScrapped = 0;
        for (Map<String, Object> m : all) {
            if ("REJECTION".equals(m.get("rowType"))) {
                long q = (Long) m.get("rejectedQty");
                balance += q;
                totalRejected += q;
            } else {
                long q = (Long) m.get("scrappedQty");
                balance -= q;
                totalScrapped += q;
            }
            m.put("pendingBalance", balance);
            m.remove("sortTime");
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> m : all) {
            LocalDate d = toDate(m.get("date"));
            if (from != null && (d == null || d.isBefore(from))) continue;
            if (to != null && (d == null || d.isAfter(to))) continue;
            if (txnType != null && !txnType.isBlank() && !txnType.equalsIgnoreCase((String) m.get("rowType"))) continue;
            rows.add(m);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("plantId", pid);
        out.put("manufacturer", plant != null ? plant.getCompanyName() : null);
        out.put("placeOfInspection", plant != null ? plant.getPlantName() : pid);
        out.put("productType", productType);
        out.put("totalRejected", totalRejected);
        out.put("totalScrapped", totalScrapped);
        out.put("pendingScrapQty", Math.max(0, totalRejected - totalScrapped));
        out.put("canScrap", scope.canScrap(pid));
        out.put("rows", rows); // chronological, like a passbook
        return out;
    }

    public Map<String, Object> getPending(Object principal, String plantId, String productType) {
        Scope scope = resolveScope(principal);
        String pid = requirePlant(plantId);
        String key = requireProduct(productType);
        assertCanSee(scope, pid);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("plantId", pid);
        m.put("productType", productType);
        m.put("pendingScrapQty", Math.max(0, computePending(pid, key)));
        m.put("canScrap", scope.canScrap(pid));
        m.put("declarationText", DECLARATION_TEXT);
        return m;
    }

    private long computePending(String plantId, String productKey) {
        long rejected = loadRejections(plantId, productKey).stream().mapToLong(e -> e.qty).sum();
        Long scrapped = scrapRepo.sumScrapped(plantId, productKey);
        return rejected - (scrapped != null ? scrapped : 0);
    }

    // =====================================================================
    // Scrap verification: view / add / edit / delete
    // =====================================================================

    public Map<String, Object> getVerification(Object principal, Long id) {
        Scope scope = resolveScope(principal);
        RailScrapVerification s = scrapRepo.findById(id).orElseThrow(() -> error("Scrap verification not found."));
        assertCanSee(scope, s.getPlantId());
        return toDetail(s, scope);
    }

    @Transactional
    public Map<String, Object> addVerification(Object principal, RailScrapVerificationRequest req) {
        Scope scope = resolveScope(principal);
        String pid = requirePlant(req.getPlantId());
        String key = requireProduct(req.getProductType());
        if (scope.userId == null || !scope.canScrap(pid)) {
            throw error("Only a Process IE mapped to this plant can record a scrap verification.");
        }
        int qty = validateQty(req.getQtyScrapped());
        if (!Boolean.TRUE.equals(req.getDeclarationAccepted())) {
            throw error("Please accept the declaration that the rejected rail pads have been cut into pieces.");
        }
        requirePhotos(req.getBeforePhotos(), "before scrapping");
        requirePhotos(req.getAfterPhotos(), "after scrapping");
        LocalDate date = req.getScrappingDate() != null ? req.getScrappingDate() : LocalDate.now();
        if (date.isAfter(LocalDate.now())) throw error("Scrapping date cannot be in the future.");

        // Serialise per plant so two submissions can't both pass the pending check
        scrapRepo.lockPlant(pid);
        long pending = computePending(pid, key);
        if (qty > pending) {
            throw error("Quantity witnessed (" + qty + ") cannot exceed the pending scrap quantity (" + Math.max(0, pending) + ").");
        }

        RailVendorPlants plant = plantMap().get(pid);
        RailScrapVerification s = new RailScrapVerification();
        s.setVerificationNo(nextVerificationNo());
        s.setPlantId(pid);
        s.setVendorCode(plant != null ? plant.getVendorCode() : null);
        s.setCompanyName(plant != null ? plant.getCompanyName() : null);
        s.setPlantName(plant != null ? plant.getPlantName() : null);
        s.setProductType(req.getProductType().trim());
        s.setScrappingDate(date);
        s.setShift(trimToNull(req.getShift()));
        s.setAvailablePendingQty((int) pending);
        s.setQtyScrapped(qty);
        s.setDeclarationAccepted(true);
        s.setDeclarationText(DECLARATION_TEXT);
        s.setRemarks(trimToNull(req.getRemarks()));
        s.setIeUserId(scope.userId);
        s.setIeName(scope.userName);
        s.setIeType(IE_TYPE_PROCESS);
        s.setStatus(RailScrapVerification.STATUS_ACTIVE);
        s.setCreatedBy(String.valueOf(scope.userId));
        s = scrapRepo.save(s);

        savePhotos(s, req.getBeforePhotos(), RailScrapVerificationImage.BEFORE);
        savePhotos(s, req.getAfterPhotos(), RailScrapVerificationImage.AFTER);
        log.info("Rail Pad scrap verification {} recorded: plant {}, {} x {} by user {}",
                s.getVerificationNo(), pid, qty, s.getProductType(), scope.userId);
        return toDetail(s, scope);
    }

    @Transactional
    public Map<String, Object> updateVerification(Object principal, Long id, RailScrapVerificationRequest req) {
        Scope scope = resolveScope(principal);
        RailScrapVerification s = getEditableOrThrow(scope, id);
        String reason = requireReason(req.getReason());
        String actor = String.valueOf(scope.userId);

        if (req.getQtyScrapped() != null && !req.getQtyScrapped().equals(s.getQtyScrapped())) {
            int qty = validateQty(req.getQtyScrapped());
            scrapRepo.lockPlant(s.getPlantId());
            // This entry's own quantity is available again when changing it
            long available = computePending(s.getPlantId(), productKey(s.getProductType())) + s.getQtyScrapped();
            if (qty > available) {
                throw error("Quantity witnessed (" + qty + ") cannot exceed the pending scrap quantity (" + Math.max(0, available) + ").");
            }
            audit(s.getId(), "EDIT", "qty_scrapped", String.valueOf(s.getQtyScrapped()), String.valueOf(qty), reason, actor);
            s.setQtyScrapped(qty);
        }
        if (req.getScrappingDate() != null && !req.getScrappingDate().equals(s.getScrappingDate())) {
            if (req.getScrappingDate().isAfter(LocalDate.now())) throw error("Scrapping date cannot be in the future.");
            audit(s.getId(), "EDIT", "scrapping_date", String.valueOf(s.getScrappingDate()), String.valueOf(req.getScrappingDate()), reason, actor);
            s.setScrappingDate(req.getScrappingDate());
        }
        if (req.getShift() != null && !Objects.equals(trimToNull(req.getShift()), s.getShift())) {
            audit(s.getId(), "EDIT", "shift", s.getShift(), trimToNull(req.getShift()), reason, actor);
            s.setShift(trimToNull(req.getShift()));
        }
        if (req.getRemarks() != null && !Objects.equals(trimToNull(req.getRemarks()), s.getRemarks())) {
            audit(s.getId(), "EDIT", "remarks", s.getRemarks(), trimToNull(req.getRemarks()), reason, actor);
            s.setRemarks(trimToNull(req.getRemarks()));
        }
        // Photos: sending photos of a type replaces that type (each type always keeps at least one)
        replacePhotos(s, req.getBeforePhotos(), RailScrapVerificationImage.BEFORE, reason, actor);
        replacePhotos(s, req.getAfterPhotos(), RailScrapVerificationImage.AFTER, reason, actor);

        s.setUpdatedBy(actor);
        s = scrapRepo.save(s);
        return toDetail(s, scope);
    }

    @Transactional
    public void deleteVerification(Object principal, Long id, String reason) {
        Scope scope = resolveScope(principal);
        RailScrapVerification s = getEditableOrThrow(scope, id);
        String why = requireReason(reason);
        String actor = String.valueOf(scope.userId);
        audit(s.getId(), "DELETE", "status", s.getStatus(), RailScrapVerification.STATUS_DELETED, why, actor);
        s.setStatus(RailScrapVerification.STATUS_DELETED);
        s.setUpdatedBy(actor);
        scrapRepo.save(s);
        log.info("Rail Pad scrap verification {} deleted by user {}: {}", s.getVerificationNo(), scope.userId, why);
    }

    private RailScrapVerification getEditableOrThrow(Scope scope, Long id) {
        RailScrapVerification s = scrapRepo.findById(id).orElseThrow(() -> error("Scrap verification not found."));
        if (RailScrapVerification.STATUS_DELETED.equals(s.getStatus())) {
            throw error("This scrap verification has already been deleted.");
        }
        if (scope.userId == null || !scope.userId.equals(s.getIeUserId())) {
            throw error("Only the engineer who recorded this scrap verification can change it.");
        }
        if (ChronoUnit.MINUTES.between(s.getCreatedDate(), LocalDateTime.now()) > EDIT_WINDOW_MINUTES) {
            throw error("This scrap verification is locked: it can only be edited or deleted within 1 hour of recording.");
        }
        return s;
    }

    private boolean isEditable(RailScrapVerification s, Scope scope) {
        return RailScrapVerification.STATUS_ACTIVE.equals(s.getStatus())
                && scope.userId != null && scope.userId.equals(s.getIeUserId())
                && s.getCreatedDate() != null
                && ChronoUnit.MINUTES.between(s.getCreatedDate(), LocalDateTime.now()) <= EDIT_WINDOW_MINUTES;
    }

    private Map<String, Object> toDetail(RailScrapVerification s, Scope scope) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("verificationNo", s.getVerificationNo());
        m.put("plantId", s.getPlantId());
        m.put("vendorCode", s.getVendorCode());
        m.put("manufacturer", s.getCompanyName());
        m.put("placeOfInspection", s.getPlantName());
        m.put("productType", s.getProductType());
        m.put("scrappingDate", s.getScrappingDate() != null ? s.getScrappingDate().toString() : null);
        m.put("shift", s.getShift());
        m.put("availablePendingQty", s.getAvailablePendingQty());
        m.put("qtyScrapped", s.getQtyScrapped());
        m.put("declarationAccepted", s.getDeclarationAccepted());
        m.put("declarationText", s.getDeclarationText());
        m.put("remarks", s.getRemarks());
        m.put("engineer", s.getIeName());
        m.put("status", s.getStatus());
        m.put("createdAt", withZone(s.getCreatedDate()));
        m.put("lockedAt", s.getCreatedDate() != null ? withZone(s.getCreatedDate().plusMinutes(EDIT_WINDOW_MINUTES)) : null);
        m.put("editable", isEditable(s, scope));

        List<String> before = new ArrayList<>(), after = new ArrayList<>();
        for (RailScrapVerificationImage img : imageRepo.findByScrapVerificationIdOrderByIdAsc(s.getId())) {
            (RailScrapVerificationImage.BEFORE.equals(img.getPhotoType()) ? before : after).add(img.getImageUrl());
        }
        m.put("beforePhotos", before);
        m.put("afterPhotos", after);

        List<Map<String, Object>> audit = new ArrayList<>();
        for (RailScrapVerificationAudit a : auditRepo.findByScrapVerificationIdOrderByActionAtDesc(s.getId())) {
            Map<String, Object> am = new LinkedHashMap<>();
            am.put("action", a.getAction());
            am.put("field", a.getFieldChanged());
            am.put("oldValue", a.getOldValue());
            am.put("newValue", a.getNewValue());
            am.put("reason", a.getReason());
            am.put("actionBy", a.getActionBy());
            am.put("actionAt", withZone(a.getActionAt()));
            audit.add(am);
        }
        m.put("auditTrail", audit);
        return m;
    }

    // =====================================================================
    // Photos
    // =====================================================================

    private void requirePhotos(List<RailScrapVerificationRequest.Photo> photos, String label) {
        long count = photos == null ? 0 : photos.stream().filter(p -> p != null && p.getBase64Data() != null && !p.getBase64Data().isBlank()).count();
        if (count == 0) throw error("Please add at least one photo " + label + ".");
        if (count > MAX_PHOTOS_PER_TYPE) throw error("A maximum of " + MAX_PHOTOS_PER_TYPE + " photos " + label + " is allowed.");
    }

    private void savePhotos(RailScrapVerification s, List<RailScrapVerificationRequest.Photo> photos, String type) {
        int n = 0;
        for (RailScrapVerificationRequest.Photo p : photos) {
            if (p == null || p.getBase64Data() == null || p.getBase64Data().isBlank()) continue;
            n++;
            String blobName = "railpad/" + s.getVerificationNo() + "/" + type + "_" + n + "_" + System.currentTimeMillis() + ".jpg";
            String url = azureBlobStorageService.uploadBase64File(p.getBase64Data(), blobName, IMAGE_CONTAINER, TARGET_IMAGE_KB);
            if (url == null || url.isBlank()) throw error("Could not upload the " + type.toLowerCase() + " photo. Please try again.");
            RailScrapVerificationImage img = new RailScrapVerificationImage();
            img.setScrapVerificationId(s.getId());
            img.setPhotoType(type);
            img.setImageUrl(url);
            img.setBlobName(blobName);
            img.setCreatedBy(s.getCreatedBy());
            imageRepo.save(img);
        }
    }

    private void replacePhotos(RailScrapVerification s, List<RailScrapVerificationRequest.Photo> photos, String type,
                               String reason, String actor) {
        if (photos == null || photos.isEmpty()) return;
        requirePhotos(photos, type.equals(RailScrapVerificationImage.BEFORE) ? "before scrapping" : "after scrapping");
        List<RailScrapVerificationImage> old = imageRepo.findByScrapVerificationIdAndPhotoType(s.getId(), type);
        audit(s.getId(), "EDIT", type.toLowerCase() + "_photos",
                String.join(", ", old.stream().map(RailScrapVerificationImage::getBlobName).toList()),
                photos.size() + " new photo(s)", reason, actor);
        imageRepo.deleteAll(old); // old blobs stay in storage as part of the audit trail
        savePhotos(s, photos, type);
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private void audit(Long id, String action, String field, String oldVal, String newVal, String reason, String actor) {
        RailScrapVerificationAudit a = new RailScrapVerificationAudit();
        a.setScrapVerificationId(id);
        a.setAction(action);
        a.setFieldChanged(field);
        a.setOldValue(oldVal);
        a.setNewValue(newVal);
        a.setReason(reason);
        a.setActionBy(actor);
        a.setActionAt(LocalDateTime.now());
        auditRepo.save(a);
    }

    private synchronized String nextVerificationNo() {
        String prefix = "RSV-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + "-";
        Long max = scrapRepo.maxSequenceForPrefix(prefix + "%");
        return prefix + String.format("%02d", (max != null ? max : 0) + 1);
    }

    private Map<String, RailVendorPlants> plantMap() {
        Map<String, RailVendorPlants> map = new HashMap<>();
        for (RailVendorPlants p : vendorPlantsRepo.findAll()) {
            String pid = normPlant(p.getPlantId());
            if (pid != null) map.putIfAbsent(pid, p);
        }
        return map;
    }

    private int validateQty(Integer qty) {
        if (qty == null || qty <= 0) throw error("Quantity witnessed for scrapping must be greater than zero.");
        return qty;
    }

    private String requireReason(String reason) {
        if (reason == null || reason.trim().length() < 5) {
            throw error("Please give a reason (at least 5 characters) for changing this scrap verification.");
        }
        return reason.trim();
    }

    private String requirePlant(String plantId) {
        String pid = normPlant(plantId);
        if (pid == null) throw error("Plant is required.");
        return pid;
    }

    private String requireProduct(String productType) {
        String key = productKey(productType);
        if (key == null) throw error("Product type is required.");
        return key;
    }

    private static InvalidInputException error(String message) {
        return new InvalidInputException(new ErrorDetails(400, 400, "error", message));
    }

    /** Plant ids / vendor codes are stored with and without a leading ':' */
    static String normPlant(String v) {
        if (v == null) return null;
        String s = v.trim().replaceAll("^:+", "");
        return s.isEmpty() ? null : s;
    }

    static String productKey(String productType) {
        if (productType == null || productType.isBlank()) return null;
        return productType.trim().toUpperCase();
    }

    private static String trimToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static String str(Object o) {
        if (o == null) return null;
        String s = o.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private static long num(Object o) {
        return o instanceof Number n ? n.longValue() : 0L;
    }

    private static String nameOr(String name, Object userId) {
        return name != null ? name : (userId != null ? "User " + userId : null);
    }

    private static LocalDate toDate(Object o) {
        if (o == null) return null;
        if (o instanceof LocalDate d) return d;
        if (o instanceof LocalDateTime dt) return dt.toLocalDate();
        if (o instanceof java.sql.Date d) return d.toLocalDate();
        if (o instanceof java.sql.Timestamp t) return t.toLocalDateTime().toLocalDate();
        if (o instanceof java.util.Date d) return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        try { return LocalDate.parse(o.toString().substring(0, 10)); } catch (Exception e) { return null; }
    }

    private static LocalDateTime toDateTime(Object o) {
        if (o == null) return null;
        if (o instanceof LocalDateTime dt) return dt;
        if (o instanceof java.sql.Timestamp t) return t.toLocalDateTime();
        if (o instanceof java.util.Date d) return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        if (o instanceof LocalDate d) return d.atStartOfDay();
        return null;
    }

    /** Server-local time with its offset, so the browser can show it in IST */
    private static String withZone(LocalDateTime t) {
        return t == null ? null : t.atZone(ZoneId.systemDefault()).toOffsetDateTime().toString();
    }
}
