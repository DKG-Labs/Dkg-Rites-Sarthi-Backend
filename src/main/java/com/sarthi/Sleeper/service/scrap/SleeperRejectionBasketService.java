package com.sarthi.Sleeper.service.scrap;

import com.sarthi.Sleeper.dto.scrap.SleeperScrapVerificationRequest;
import com.sarthi.Sleeper.entity.SleeperPoiIeMapping;
import com.sarthi.Sleeper.entity.VendorPlant;
import com.sarthi.Sleeper.entity.scrap.SleeperScrapVerification;
import com.sarthi.Sleeper.entity.scrap.SleeperScrapVerificationAudit;
import com.sarthi.Sleeper.entity.scrap.SleeperScrapVerificationImage;
import com.sarthi.Sleeper.entity.scrap.SleeperScrapVerificationItem;
import com.sarthi.Sleeper.repository.SleeperPoiIeMappingRepository;
import com.sarthi.Sleeper.repository.VendorPlantRepository;
import com.sarthi.Sleeper.repository.scrap.SleeperScrapVerificationAuditRepository;
import com.sarthi.Sleeper.repository.scrap.SleeperScrapVerificationImageRepository;
import com.sarthi.Sleeper.repository.scrap.SleeperScrapVerificationItemRepository;
import com.sarthi.Sleeper.repository.scrap.SleeperScrapVerificationRepository;
import com.sarthi.entity.UserMaster;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.exception.InvalidInputException;
import com.sarthi.service.AzureBlobStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Concrete Sleeper Rejection Basket, Scrap Verification and Scrap Ledger (SRS - Rejection Basket & Scrap Verification).
 *
 * Rejections are read live, one row per uniquely identified sleeper, from demoulding (in-process) inspections and
 * final inspection (including destructive test failures logged there). A sleeper rejected at more than one stage is
 * counted once, at its first rejection. Scrap verifications record the exact sleepers destroyed; a sleeper stays
 * in the pending basket until an active verification covers it.
 */
@Service
public class SleeperRejectionBasketService {

    private static final Logger log = LoggerFactory.getLogger(SleeperRejectionBasketService.class);

    /** Exactly the SRS declaration the IE ticks on the Scrap Verification form (saved with every entry) */
    public static final String DECLARATION_TEXT = "I certify that the selected sleepers have been permanently damaged.";

    public static final String STAGE_DEMOULDING = "DEMOULDING";
    public static final String STAGE_FINAL = "FINAL";
    public static final String STAGE_TESTING = "TESTING";

    private static final int EDIT_WINDOW_MINUTES = 60;
    private static final int RECENT_DAYS = 30;
    private static final String IMAGE_CONTAINER = "scrap-images";
    private static final int TARGET_IMAGE_KB = 1400;
    private static final int MAX_PHOTOS_PER_TYPE = 3;
    private static final int MAX_SLEEPERS_PER_ENTRY = 1000;

    /** MR (Moment of Resistance) failure named in a final-inspection rejection: counts, as a test failure */
    private static final Pattern MR_FAILURE = Pattern.compile("\\b(MR|MOMENT OF RESISTANCE)\\b", Pattern.CASE_INSENSITIVE);
    /**
     * Other destructive tests (SBT, MF, MOR...). Client decision: only MR rejections are considered for disposal,
     * so a final-inspection rejection naming one of these (and not MR) is left out of the basket.
     */
    private static final Pattern OTHER_TEST_FAILURE = Pattern.compile(
            "\\b(SBT|MF|MOR|MODULUS OF FAILURE|MODULUS OF RUPTURE|STATIC BENDING|BENDING TEST|FLEXURAL)\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern IE_WORD = Pattern.compile("\\bIE\\b");
    /** Demoulding sleeper numbers that are only a mould sequence letter (A-H) and need the bench to be unique */
    private static final Pattern SEQUENCE_ONLY = Pattern.compile("(?i)[A-Z]");

    /** Roles with read-only access to every plant (SRS: RITES Admin / Railway Board); anyone else is denied */
    private static final Set<String> READ_ALL_ROLES = Set.of(
            "RAILWAY BOARD", "ZONAL RAILWAY", "CM", "CONTROL MANAGER", "CONTROLLING MANAGER", "SBU HEAD",
            "ADMIN", "SUPER ADMIN", "RITES ADMIN");

    /** Placeholder for an IN (...) list that must not be empty */
    private static final String NO_MATCH = "__NO_MATCHING_CALL__";

    /** Rows per multi-row INSERT of scrapped sleepers */
    private static final int INSERT_CHUNK = 200;

    @Autowired private SleeperScrapVerificationRepository scrapRepo;
    @Autowired private SleeperScrapVerificationItemRepository itemRepo;
    @Autowired private SleeperScrapVerificationImageRepository imageRepo;
    @Autowired private SleeperScrapVerificationAuditRepository auditRepo;
    @Autowired private VendorPlantRepository vendorPlantRepo;
    @Autowired private SleeperPoiIeMappingRepository poiIeMappingRepo;
    @Autowired private AzureBlobStorageService azureBlobStorageService;
    @Autowired private JdbcTemplate jdbcTemplate;

    // =====================================================================
    // Access scope
    // =====================================================================

    /** Which plants the logged-in user may see, and where they may record scrap verifications. */
    private static final class Scope {
        boolean allPlants;
        final Set<String> plantIds = new HashSet<>();
        final Set<String> scrapPlantIds = new HashSet<>();
        final Map<String, String> ieTypeByPlant = new HashMap<>();
        String roleIeType;
        Long userId;
        String userName;

        boolean canSee(String plantId) { return allPlants || plantIds.contains(plantId); }
        boolean canScrap(String plantId) { return scrapPlantIds.contains(plantId); }
        String ieTypeFor(String plantId) { return ieTypeByPlant.getOrDefault(plantId, roleIeType); }
    }

    /**
     * Deny by default: IEs see (and scrap for) their mapped plants, READ_ALL_ROLES see every plant read-only,
     * and any other user sees only plants registered to them in vendor_plant (vendors), read-only.
     * Users owning no plant and unknown principal types get an empty scope.
     */
    private Scope resolveScope(Object principal) {
        Scope scope = new Scope();
        if (!(principal instanceof UserMaster user)) {
            // Service / mock-token logins (e.g. Railway Board) carry their role as a ROLE_ authority
            if (principal instanceof UserDetails details) {
                for (GrantedAuthority a : details.getAuthorities()) {
                    if (a != null && READ_ALL_ROLES.contains(normRole(a.getAuthority()))) scope.allPlants = true;
                }
            }
            if (!scope.allPlants) log.debug("Sleeper scrap: no access for principal type {}", principal == null ? null : principal.getClass().getSimpleName());
            return scope;
        }
        scope.userId = user.getUserId() != null ? user.getUserId().longValue() : null;
        scope.userName = user.getFullName() != null && !user.getFullName().isBlank() ? user.getFullName() : user.getUsername();
        String role = normRole(user.getRoleName());
        scope.roleIeType = role.contains("PROCESS") ? "PROCESS_IE" : "MAIN_IE";

        List<SleeperPoiIeMapping> mappings = user.getUserId() != null
                ? poiIeMappingRepo.findByIeUserId(user.getUserId()) : List.of();
        if (!mappings.isEmpty() || IE_WORD.matcher(role).find()) {
            // Process IE and Main IE: their mapped plants, where they can also record scrap verifications
            for (SleeperPoiIeMapping m : mappings) {
                String pid = normPlant(m.getPlantId());
                if (pid == null) continue;
                scope.plantIds.add(pid);
                scope.scrapPlantIds.add(pid);
                scope.ieTypeByPlant.putIfAbsent(pid, ieType(m.getIeType(), scope.roleIeType));
            }
            return scope;
        }
        // role_name may hold several comma-separated roles
        boolean readAll = Arrays.stream(Objects.toString(user.getRoleName(), "").split(","))
                .map(SleeperRejectionBasketService::normRole).anyMatch(READ_ALL_ROLES::contains);
        if (readAll) {
            scope.allPlants = true; // RITES Admin, CM, Railway Board: read-only
            return scope;
        }
        // Vendors (whatever their role is named): only the plants registered to them in vendor_plant, read-only.
        // Vendor code is stored with and without a leading ':'. A user owning no plant gets nothing.
        List<VendorPlant> own = new ArrayList<>();
        if (scope.userId != null) own.addAll(vendorPlantRepo.findByVendorId(scope.userId));
        for (String code : new LinkedHashSet<>(Arrays.asList(normPlant(user.getEmployeeCode()), normPlant(user.getUsername())))) {
            if (code == null) continue;
            own.addAll(vendorPlantRepo.findByVendorCode(code));
            own.addAll(vendorPlantRepo.findByVendorCode(":" + code));
        }
        for (VendorPlant p : own) {
            if (normPlant(p.getPlantId()) != null) scope.plantIds.add(normPlant(p.getPlantId()));
        }
        if (scope.plantIds.isEmpty()) {
            log.debug("Sleeper scrap: no access for role '{}' (user {})", user.getRoleName(), scope.userId);
        }
        return scope;
    }

    /** "ROLE_RAILWAY_BOARD", "Railway_Board", "Rites Admin" -> "RAILWAY BOARD", "RITES ADMIN" */
    static String normRole(String role) {
        if (role == null) return "";
        return role.trim().toUpperCase().replaceFirst("^ROLE_", "").replace('_', ' ').replaceAll("\\s+", " ");
    }

    private static String ieType(String mappingType, String fallback) {
        if (mappingType == null) return fallback;
        String t = mappingType.toUpperCase();
        if (t.contains("PROCESS")) return "PROCESS_IE";
        if (t.contains("MAIN")) return "MAIN_IE";
        return fallback;
    }

    private void assertCanSee(Scope scope, String plantId) {
        if (!scope.canSee(plantId)) {
            throw error("You do not have access to this plant's rejection basket.");
        }
    }

    // =====================================================================
    // Rejected sleepers (live)
    // =====================================================================

    /** One rejected sleeper. */
    private static final class Rejection {
        String plantId, vendorCode, batchNo, sleeperNo, key, sleeperType, stage, reason, addedBy, shift, location, reference;
        LocalDate date, castingDate;
        LocalDateTime createdAt;

        String plantKey() { return plantId + "|" + key; }

        Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("batchNo", batchNo);
            m.put("sleeperNo", sleeperNo);
            m.put("sleeperType", sleeperType);
            m.put("stage", stage);
            m.put("stageLabel", stageLabel(stage));
            m.put("reason", reason);
            m.put("rejectionDate", date != null ? date.toString() : null);
            m.put("castingDate", castingDate != null ? castingDate.toString() : null);
            m.put("shift", shiftLabel(shift));
            m.put("location", location);
            m.put("reference", reference);
            String role = ieRoleForRejection(stage, shift);
            m.put("addedBy", engineerLabel(addedBy, role));
            m.put("addedByName", addedBy);
            m.put("ieRole", role);
            return m;
        }
    }

    private static final Comparator<Rejection> CHRONO =
            Comparator.comparing((Rejection r) -> r.date, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(r -> r.createdAt, Comparator.nullsLast(Comparator.naturalOrder()));

    /** Rejected sleepers (each once, oldest first) plus demoulding defects that could not be identified */
    private static final class Loaded {
        final List<Rejection> list;
        /** Demoulding defects skipped because their sleeper ID would be ambiguous (no bench number) */
        final Map<String, Integer> unidentifiedByPlant;

        Loaded(List<Rejection> list, Map<String, Integer> unidentifiedByPlant) {
            this.list = list;
            this.unidentifiedByPlant = unidentifiedByPlant;
        }

        int unidentified(String plantId) { return unidentifiedByPlant.getOrDefault(plantId, 0); }
    }

    /**
     * Demoulding sleeper ID: the recorded sleeper no., or bench + mould sequence when only the sequence letter
     * was recorded. Returns null when the bench is unknown, since "A" alone would merge different sleepers.
     */
    static String demouldingSleeperNo(String bench, String seq, String no) {
        boolean sequenceOnly = no == null || (seq != null && no.equalsIgnoreCase(seq)) || SEQUENCE_ONLY.matcher(no).matches();
        if (!sequenceOnly) return no;
        String s = seq != null ? seq : no;
        if (bench == null || s == null) return null;
        return bench + "-" + s;
    }

    // ---------- Timing (diagnostics: logs where a slow request spends its time) ----------

    /** Per-request query timings, in ms, in call order */
    private static final ThreadLocal<Map<String, long[]>> TIMINGS = new ThreadLocal<>();
    private static final long SLOW_REQUEST_MS = 2000;

    private static <T> T timed(String step, java.util.function.Supplier<T> call) {
        long start = System.nanoTime();
        T result = call.get();
        Map<String, long[]> t = TIMINGS.get();
        if (t != null) {
            long[] v = t.computeIfAbsent(step, k -> new long[2]);
            v[0] += (System.nanoTime() - start) / 1_000_000;
            v[1] += result instanceof Collection<?> c ? c.size() : 0;
        }
        return result;
    }

    private static void startTimings() { TIMINGS.set(new LinkedHashMap<>()); }

    /** Logs the step timings (INFO when the request was slow) and clears them */
    private static void logTimings(String what, long startNanos) {
        Map<String, long[]> t = TIMINGS.get();
        TIMINGS.remove();
        long total = (System.nanoTime() - startNanos) / 1_000_000;
        if (t == null) return;
        String detail = t.entrySet().stream()
                .map(en -> en.getKey() + "=" + en.getValue()[0] + "ms/" + en.getValue()[1] + " rows")
                .collect(Collectors.joining(", "));
        if (total >= SLOW_REQUEST_MS) log.info("Sleeper scrap {} took {} ms: {}", what, total, detail);
        else log.debug("Sleeper scrap {} took {} ms: {}", what, total, detail);
    }

    /** All rejected sleepers of one plant (empty = all plants), each sleeper once, oldest first */
    private Loaded loadRejections(String plantId) {
        String filter = plantId == null ? "" : plantId;
        Map<String, Integer> unidentified = new HashMap<>();
        List<Rejection> demoulding = new ArrayList<>();
        for (Object[] r : timed("demoulding", () -> scrapRepo.findDemouldingRejections(filter))) {
            Rejection e = new Rejection();
            e.plantId = normPlant(str(r[8]));
            e.vendorCode = str(r[9]);
            e.batchNo = str(r[10]);
            String bench = str(r[1]) != null ? str(r[1]) : str(r[11]);
            String no = demouldingSleeperNo(bench, str(r[2]), str(r[3]));
            if (no == null) {
                if (e.plantId != null) unidentified.merge(e.plantId, 1, Integer::sum);
                continue;
            }
            e.sleeperNo = no;
            e.castingDate = toDate(r[12]);
            e.date = toDate(r[13]) != null ? toDate(r[13]) : (e.castingDate != null ? e.castingDate : toDate(r[19]));
            e.sleeperType = str(r[14]);
            e.shift = str(r[15]);
            e.location = str(r[16]);
            e.addedBy = nameOr(str(r[18]), r[17]);
            e.createdAt = toDateTime(r[19]) != null ? toDateTime(r[19]) : toDateTime(r[6]);
            e.stage = STAGE_DEMOULDING;
            List<String> reasons = new ArrayList<>();
            if (str(r[4]) != null) reasons.add(str(r[4]));
            if (str(r[5]) != null) reasons.add(str(r[5]));
            e.reason = String.join("; ", reasons);
            e.reference = "Batch " + Objects.toString(e.batchNo, "-");
            if (e.plantId == null || e.sleeperNo == null) continue;
            e.key = sleeperKey(e.batchNo, e.sleeperNo);
            demoulding.add(e);
        }

        // Final rejections belong to the plant of their inspection call (as in ERC), not the plant saved on the
        // batch row, which can be a placeholder (e.g. "PLANT-01" when the screen had no plant)
        List<String> plantCalls = filter.isEmpty() ? List.of() : timed("plantCalls", () -> scrapRepo.findCallNosByPlant(filter));
        List<String> callFilter = plantCalls.isEmpty() ? List.of(NO_MATCH) : plantCalls;
        List<Object[]> finalRows = timed("finalRejections", () -> scrapRepo.findFinalRejections(filter, callFilter));
        List<Object[]> finalEtRows = timed("finalEpoxy", () -> scrapRepo.findFinalEpoxySleepers(filter, callFilter));
        Set<String> callNos = new HashSet<>();
        for (Object[] r : finalRows) if (str(r[8]) != null) callNos.add(str(r[8]));
        for (Object[] r : finalEtRows) if (str(r[3]) != null) callNos.add(str(r[3]));
        Map<String, Object[]> resultByCall = new HashMap<>();
        Map<String, Object[]> callByNo = new HashMap<>();
        if (!callNos.isEmpty()) {
            for (Object[] r : timed("finalResults", () -> scrapRepo.findFinalResultInfo(callNos))) {
                if (str(r[0]) != null) resultByCall.putIfAbsent(str(r[0]), r);
            }
            for (Object[] r : timed("calls", () -> scrapRepo.findCallInfo(callNos))) {
                if (str(r[0]) != null) callByNo.putIfAbsent(str(r[0]), r);
            }
        }
        // Rows whose call is unknown fall back to the batch row's plant, but only if it is a registered plant
        Set<String> fallbackPlants = new HashSet<>();
        for (Object[] r : finalRows) {
            Object[] call = str(r[8]) != null ? callByNo.get(str(r[8])) : null;
            if ((call == null || normPlant(str(call[1])) == null) && normPlant(str(r[5])) != null) fallbackPlants.add(normPlant(str(r[5])));
        }
        Set<String> registeredFallbacks = plantsFor(fallbackPlants).keySet();

        int unassigned = 0;
        int otherTests = 0;
        List<Rejection> finals = new ArrayList<>();
        for (Object[] r : finalRows) {
            Rejection e = new Rejection();
            String callNo = str(r[8]);
            Object[] call = callNo != null ? callByNo.get(callNo) : null;
            String plant = call != null ? normPlant(str(call[1])) : null;
            if (plant == null) {
                String saved = normPlant(str(r[5]));
                plant = saved != null && registeredFallbacks.contains(saved) ? saved : null;
            }
            if (plant == null) { unassigned++; continue; }
            if (!filter.isEmpty() && !filter.equals(plant)) continue; // batch saved under this plant, call is elsewhere
            e.plantId = plant;
            e.vendorCode = str(r[6]);
            e.batchNo = str(r[7]);
            e.sleeperNo = str(r[1]);
            Object[] result = callNo != null ? resultByCall.get(callNo) : null;
            e.sleeperType = result != null && str(result[1]) != null ? str(result[1]) : (call != null ? str(call[2]) : null);
            e.castingDate = toDate(r[9]);
            e.createdAt = toDateTime(r[13]);
            LocalDate inspected = result != null ? toDate(result[2]) : null;
            e.date = inspected != null ? inspected : (e.createdAt != null ? e.createdAt.toLocalDate() : e.castingDate);
            e.shift = str(r[10]);
            e.addedBy = nameOr(str(r[12]), r[11]);
            String reason = str(r[2]);
            String type = str(r[3]);
            e.reason = reason != null ? reason : "Rejected at final inspection";
            String text = Objects.toString(reason, "") + " " + Objects.toString(type, "");
            boolean mr = MR_FAILURE.matcher(text).find();
            if (!mr && OTHER_TEST_FAILURE.matcher(text).find()) { otherTests++; continue; } // only MR counts
            e.stage = mr ? STAGE_TESTING : STAGE_FINAL;
            e.location = callNo != null ? "Call " + callNo : null;
            e.reference = "Batch " + Objects.toString(e.batchNo, "-");
            if (e.plantId == null || e.sleeperNo == null) continue;
            e.key = sleeperKey(e.batchNo, e.sleeperNo);
            finals.add(e);
        }

        finals.addAll(loadProcessFinalRejections(filter));
        List<Rejection> mrFailures = loadMrFailures(filter);
        Set<String> epoxyKeys = loadEpoxyKeys(filter, finalEtRows, callByNo);

        demoulding.sort(CHRONO);
        finals.sort(CHRONO);
        mrFailures.sort(CHRONO);
        // Each sleeper once, at its first rejection: demoulding, then final inspection, then MR test.
        // An epoxy-treated sleeper is not a rejection (client decision): its demoulding defect was repaired. If it is
        // rejected again later (final inspection or MR test), that later rejection still counts.
        Map<String, Rejection> unique = new LinkedHashMap<>();
        int epoxyCleared = 0;
        for (Rejection e : demoulding) {
            if (epoxyKeys.contains(e.plantKey())) { epoxyCleared++; continue; }
            unique.putIfAbsent(e.plantKey(), e);
        }
        for (Rejection e : finals) unique.putIfAbsent(e.plantKey(), e);
        for (Rejection e : mrFailures) unique.putIfAbsent(e.plantKey(), e);
        List<Rejection> list = new ArrayList<>(unique.values());
        list.sort(CHRONO);
        if (!unidentified.isEmpty()) {
            log.warn("Sleeper scrap: skipped demoulding defect rows without a bench number (sleeper ID ambiguous): {}", unidentified);
        }
        if (unassigned > 0) {
            log.warn("Sleeper scrap: skipped {} final rejection rows whose plant could not be found (no matching inspection call "
                    + "and no registered plant on the batch, e.g. 'PLANT-01')", unassigned);
        }
        if (otherTests > 0 || epoxyCleared > 0) {
            log.debug("Sleeper scrap: left out {} non-MR test rejections and {} epoxy-treated demoulding defects", otherTests, epoxyCleared);
        }
        return new Loaded(list, unidentified);
    }

    /** Rejections on the Process IE Visual Check / Critical / Non-Critical Dimensions forms (inspection_test_result) */
    private List<Rejection> loadProcessFinalRejections(String filter) {
        Map<String, Rejection> bySleeper = new LinkedHashMap<>();
        for (Object[] r : timed("processFinalRejections", () -> scrapRepo.findProcessFinalRejections(filter))) {
            String plant = normPlant(str(r[4]));
            String no = str(r[1]) != null ? str(r[1]).trim() : null;
            if (plant == null || no == null) continue;
            Rejection e = new Rejection();
            e.plantId = plant;
            e.vendorCode = str(r[5]);
            e.batchNo = str(r[6]);
            e.sleeperNo = no;
            e.castingDate = toDate(r[7]);
            e.sleeperType = str(r[8]);
            e.shift = str(r[9]);
            e.createdAt = toDateTime(r[13]);
            LocalDate tested = toDate(r[10]);
            e.date = tested != null ? tested : (e.createdAt != null ? e.createdAt.toLocalDate() : e.castingDate);
            e.addedBy = nameOr(str(r[12]), r[11]);
            e.stage = STAGE_FINAL;
            e.reason = str(r[2]) != null ? str(r[2]) : "Rejected at final inspection";
            String module = Objects.toString(r[3], "");
            e.location = switch (module) {
                case "1" -> "Visual Check & Measurement";
                case "2" -> "Critical Dimensions";
                case "3" -> "Non-Critical Dimensions";
                default -> "Final Inspection";
            };
            e.reference = "Batch " + Objects.toString(e.batchNo, "-");
            e.key = sleeperKey(e.batchNo, e.sleeperNo);
            // Later forms copy an earlier rejection as "Previously Rejected": keep one row per sleeper, with the
            // real defect reason (and the form it was found on) whenever one exists
            Rejection prev = bySleeper.get(e.plantKey());
            if (prev == null || (isCarriedOver(prev.reason) && !isCarriedOver(e.reason))) {
                bySleeper.put(e.plantKey(), e);
            }
        }
        return new ArrayList<>(bySleeper.values());
    }

    private static boolean isCarriedOver(String reason) {
        return reason == null || reason.trim().equalsIgnoreCase("Previously Rejected");
    }

    /** One rejection per sleeper of every failed MR test (a test may cover a comma-separated list of sleepers) */
    private List<Rejection> loadMrFailures(String filter) {
        List<Rejection> out = new ArrayList<>();
        for (Object[] r : timed("mrFailures", () -> scrapRepo.findMrFailures(filter))) {
            String plant = normPlant(str(r[7]));
            if (plant == null) continue;
            String[] nos = Objects.toString(str(r[6]), "").split("\\s*,\\s*");
            String[] benches = Objects.toString(str(r[5]), "").split("\\s*,\\s*");
            LocalDate tested = parseFlexDate(r[4]);
            LocalDateTime created = toDateTime(r[11]);
            for (int i = 0; i < nos.length; i++) {
                String bench = i < benches.length && !benches[i].isBlank() ? benches[i] : (benches.length > 0 && !benches[0].isBlank() ? benches[0] : null);
                String no = demouldingSleeperNo(bench, null, nos[i].isBlank() ? null : nos[i].trim());
                if (no == null) continue;
                Rejection e = new Rejection();
                e.plantId = plant;
                e.batchNo = str(r[1]);
                e.sleeperNo = no;
                e.sleeperType = str(r[2]);
                e.castingDate = parseFlexDate(r[3]);
                e.date = tested != null ? tested : (created != null ? created.toLocalDate() : null);
                e.createdAt = created;
                e.shift = str(r[8]);
                e.addedBy = nameOr(str(r[10]), r[9]);
                e.stage = STAGE_TESTING;
                e.reason = "MR test failed";
                e.location = "MR Test";
                e.reference = "Batch " + Objects.toString(e.batchNo, "-");
                e.key = sleeperKey(e.batchNo, e.sleeperNo);
                out.add(e);
            }
        }
        return out;
    }

    /** plant|sleeper keys of every epoxy-treated sleeper (process epoxy records and final-inspection ET list) */
    private Set<String> loadEpoxyKeys(String filter, List<Object[]> finalEtRows, Map<String, Object[]> callByNo) {
        Set<String> keys = new HashSet<>();
        for (Object[] r : timed("processEpoxy", () -> scrapRepo.findEpoxyTreated(filter))) {
            String plant = normPlant(str(r[0]));
            if (plant != null && str(r[2]) != null) keys.add(plant + "|" + sleeperKey(str(r[1]), str(r[2])));
        }
        for (Object[] r : finalEtRows) {
            Object[] call = str(r[3]) != null ? callByNo.get(str(r[3])) : null;
            String plant = call != null && normPlant(str(call[1])) != null ? normPlant(str(call[1])) : normPlant(str(r[0]));
            if (plant != null && str(r[2]) != null) keys.add(plant + "|" + sleeperKey(str(r[1]), str(r[2])));
        }
        return keys;
    }

    /** Dates stored as text in the test tables: yyyy-MM-dd, dd-MM-yyyy or dd/MM/yyyy (optionally with a time) */
    static LocalDate parseFlexDate(Object o) {
        LocalDate d = toDate(o);
        if (d != null || o == null) return d;
        String s = o.toString().trim();
        if (s.length() > 10) s = s.substring(0, 10);
        for (String p : new String[]{"dd-MM-yyyy", "dd/MM/yyyy", "yyyy/MM/dd"}) {
            try { return LocalDate.parse(s, DateTimeFormatter.ofPattern(p)); } catch (Exception ignored) { }
        }
        return null;
    }

    /** Active scrapped sleepers of the given verifications (one query) */
    private List<SleeperScrapVerificationItem> activeItems(List<SleeperScrapVerification> scraps) {
        if (scraps.isEmpty()) return List.of();
        List<Long> ids = scraps.stream().map(SleeperScrapVerification::getId).toList();
        return timed("scrappedSleepers", () -> itemRepo.findByScrapVerificationIdInAndActiveKeyIsNotNull(ids));
    }

    private static Set<String> keysOf(List<SleeperScrapVerificationItem> items) {
        return items.stream().map(SleeperScrapVerificationItem::getActiveKey).collect(Collectors.toCollection(HashSet::new));
    }

    // =====================================================================
    // Rejection Basket (plant-wise summary)
    // =====================================================================

    public List<Map<String, Object>> getBasket(Object principal) {
        long start = System.nanoTime();
        startTimings();
        try {
            return buildBasket(principal);
        } finally {
            logTimings("basket", start);
        }
    }

    private List<Map<String, Object>> buildBasket(Object principal) {
        Scope scope = timed("scope", () -> resolveScope(principal));
        if (!scope.allPlants && scope.plantIds.isEmpty()) return List.of();
        Loaded loaded = loadRejections(null);
        List<Rejection> rejections = loaded.list;
        List<SleeperScrapVerification> scraps = timed("scrapEntries", () -> scrapRepo.findActive(""));
        Set<String> scrappedKeys = keysOf(activeItems(scraps));
        LocalDate since = LocalDate.now().minusDays(RECENT_DAYS - 1L);

        Map<String, long[]> counts = new LinkedHashMap<>(); // rejected, rejected30, pending, scrapped
        Map<String, LocalDate> lastScrap = new HashMap<>();
        for (Rejection e : rejections) {
            long[] c = counts.computeIfAbsent(e.plantId, k -> new long[4]);
            c[0]++;
            if (e.date != null && !e.date.isBefore(since)) c[1]++;
            if (!scrappedKeys.contains(e.plantKey())) c[2]++;
        }
        for (SleeperScrapVerification s : scraps) {
            long[] c = counts.computeIfAbsent(s.getPlantId(), k -> new long[4]);
            c[3] += s.getQtyScrapped() != null ? s.getQtyScrapped() : 0;
            LocalDate d = s.getScrappingDate();
            if (d != null && (lastScrap.get(s.getPlantId()) == null || d.isAfter(lastScrap.get(s.getPlantId())))) {
                lastScrap.put(s.getPlantId(), d);
            }
        }
        // IEs and vendors always see their own plants, even before the first rejection
        for (String pid : scope.plantIds) counts.computeIfAbsent(pid, k -> new long[4]);
        counts.keySet().removeIf(pid -> !scope.canSee(pid));

        Map<String, VendorPlant> plants = plantsFor(counts.keySet());
        Map<String, Long> production = productionSince(since, null);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map.Entry<String, long[]> en : counts.entrySet()) {
            String pid = en.getKey();
            long[] c = en.getValue();
            VendorPlant plant = plants.get(pid);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("plantId", pid);
            m.put("vendorCode", plant != null ? plant.getVendorCode() : null);
            m.put("manufacturer", plant != null ? plant.getCompanyName() : null);
            m.put("placeOfInspection", plant != null && plant.getPlantName() != null ? plant.getPlantName() : pid);
            m.put("productionLast30Days", production.getOrDefault(pid, 0L));
            m.put("totalRejected", c[0]);
            m.put("rejectedLast30Days", c[1]);
            m.put("totalScrapped", c[3]);
            m.put("pendingScrapQty", c[2]);
            m.put("unidentifiedDefects", loaded.unidentified(pid));
            m.put("lastScrapDate", lastScrap.get(pid) != null ? lastScrap.get(pid).toString() : null);
            m.put("canScrap", scope.canScrap(pid));
            m.put("ieType", scope.canScrap(pid) ? scope.ieTypeFor(pid) : null);
            out.add(m);
        }
        out.sort(Comparator.comparing((Map<String, Object> m) -> Objects.toString(m.get("manufacturer"), ""))
                .thenComparing(m -> Objects.toString(m.get("placeOfInspection"), "")));
        return out;
    }

    // =====================================================================
    // Pending sleepers (detail grid)
    // =====================================================================

    public Map<String, Object> getPending(Object principal, String plantId) {
        Scope scope = resolveScope(principal);
        String pid = requirePlant(plantId);
        assertCanSee(scope, pid);
        Loaded loaded = loadRejections(pid);
        List<Map<String, Object>> rows = pendingRows(pendingOf(loaded, keysOf(activeItems(scrapRepo.findActive(pid)))));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("plantId", pid);
        m.put("pendingScrapQty", rows.size());
        m.put("unidentifiedDefects", loaded.unidentified(pid));
        m.put("canScrap", scope.canScrap(pid));
        m.put("declarationText", DECLARATION_TEXT);
        m.put("sleepers", rows);
        return m;
    }

    /** Rejected sleepers of the plant not covered by an active scrap verification, by plant|key */
    private Map<String, Rejection> pendingRejections(String plantId) {
        return pendingOf(loadRejections(plantId), keysOf(activeItems(scrapRepo.findActive(plantId))));
    }

    private static Map<String, Rejection> pendingOf(Loaded loaded, Set<String> scrappedKeys) {
        Map<String, Rejection> pending = new LinkedHashMap<>();
        for (Rejection e : loaded.list) {
            if (!scrappedKeys.contains(e.plantKey())) pending.put(e.plantKey(), e);
        }
        return pending;
    }

    /** Pending sleepers for the grid, newest first */
    private static List<Map<String, Object>> pendingRows(Map<String, Rejection> pending) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Rejection e : pending.values()) rows.add(e.toMap());
        Collections.reverse(rows);
        return rows;
    }

    // =====================================================================
    // Scrap ledger (running passbook)
    // =====================================================================

    public Map<String, Object> getLedger(Object principal, String plantId, LocalDate from, LocalDate to,
                                         String txnType, String batchNo, String sleeperId) {
        long start = System.nanoTime();
        startTimings();
        try {
            return buildLedger(principal, plantId, from, to, txnType, batchNo, sleeperId);
        } finally {
            logTimings("ledger " + plantId, start);
        }
    }

    private Map<String, Object> buildLedger(Object principal, String plantId, LocalDate from, LocalDate to,
                                            String txnType, String batchNo, String sleeperId) {
        Scope scope = timed("scope", () -> resolveScope(principal));
        String pid = requirePlant(plantId);
        assertCanSee(scope, pid);
        VendorPlant plant = plantFor(pid);

        Loaded loaded = loadRejections(pid);
        List<Rejection> rejections = loaded.list;
        List<SleeperScrapVerification> scraps = timed("scrapEntries", () -> scrapRepo.findActive(pid));
        List<SleeperScrapVerificationItem> activeItems = activeItems(scraps); // one query, reused below
        Map<String, Rejection> pendingMap = pendingOf(loaded, keysOf(activeItems));
        long pending = pendingMap.size();

        // Credits: one row per date + shift + stage (client: date & shift-wise rejection with reasons), listing
        // its sleepers and a reason-wise breakup
        Map<String, List<Rejection>> groups = new LinkedHashMap<>();
        for (Rejection e : rejections) {
            String key = (e.date != null ? e.date.toString() : "-") + "|" + Objects.toString(shiftLabel(e.shift), "-") + "|" + e.stage;
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(e);
        }

        List<Map<String, Object>> all = new ArrayList<>();
        for (Map.Entry<String, List<Rejection>> en : groups.entrySet()) {
            List<Rejection> group = en.getValue();
            Rejection first = group.get(0);
            String shift = shiftLabel(first.shift);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("rowType", "REJECTION");
            m.put("entryId", "R|" + en.getKey());
            m.put("entryDate", first.date != null ? first.date.toString() : null);
            m.put("date", m.get("entryDate"));
            m.put("sortTime", first.createdAt);
            m.put("transactionType", "Rejection (" + stageLabel(first.stage) + ")");
            m.put("stage", first.stage);
            m.put("stageOfInspection", stageLabel(first.stage));
            m.put("shift", shift != null ? shift : "—");
            String places = group.stream().map(r -> r.location).filter(Objects::nonNull).distinct().collect(Collectors.joining(", "));
            m.put("unitLineShedNo", !places.isEmpty() ? places : placeName(plant, pid));
            m.put("batchNo", group.stream().map(r -> r.batchNo).filter(Objects::nonNull).distinct().collect(Collectors.joining(", ")));
            m.put("reference", first.reference);
            m.put("sleeperIds", group.stream().map(r -> r.sleeperNo).toList());
            m.put("sleepers", group.stream().map(Rejection::toMap).toList());
            List<Map<String, Object>> breakup = reasonBreakup(group);
            m.put("reasonBreakup", breakup);
            m.put("remarks", breakup.stream().map(b -> b.get("reason") + " × " + b.get("count")).collect(Collectors.joining("; ")));
            m.put("rejectedQty", (long) group.size());
            m.put("scrappedQty", null);
            String role = ieRoleForRejection(first.stage, first.shift);
            m.put("ieName", group.stream().map(r -> engineerLabel(r.addedBy, ieRoleForRejection(r.stage, r.shift)))
                    .filter(Objects::nonNull).distinct().collect(Collectors.joining(", ")));
            m.put("engineer", first.addedBy);
            m.put("ieRole", role);
            m.put("scrapVerificationId", null);
            m.put("imageUrls", Collections.emptyList());
            m.put("editable", false);
            all.add(m);
        }

        // Debits: one row per scrap verification
        List<Long> scrapIds = scraps.stream().map(SleeperScrapVerification::getId).toList();
        Map<Long, List<SleeperScrapVerificationItem>> itemsById = new HashMap<>();
        Map<Long, List<String>> imagesById = new HashMap<>();
        for (SleeperScrapVerificationItem it : activeItems) {
            itemsById.computeIfAbsent(it.getScrapVerificationId(), k -> new ArrayList<>()).add(it);
        }
        if (!scrapIds.isEmpty()) {
            for (SleeperScrapVerificationImage img : imageRepo.findByScrapVerificationIdInOrderByIdAsc(scrapIds)) {
                imagesById.computeIfAbsent(img.getScrapVerificationId(), k -> new ArrayList<>()).add(img.getImageUrl());
            }
        }
        for (SleeperScrapVerification s : scraps) {
            List<SleeperScrapVerificationItem> items = itemsById.getOrDefault(s.getId(), List.of());
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("rowType", "SCRAP");
            m.put("entryId", s.getId());
            m.put("entryDate", s.getScrappingDate() != null ? s.getScrappingDate().toString() : null);
            m.put("date", m.get("entryDate"));
            m.put("sortTime", s.getCreatedDate());
            m.put("transactionType", "Scrap Verification");
            m.put("stage", null);
            m.put("stageOfInspection", "—");
            m.put("shift", s.getShift() != null ? s.getShift() : "—");
            m.put("unitLineShedNo", placeName(plant, pid));
            m.put("batchNo", items.stream().map(SleeperScrapVerificationItem::getBatchNo).filter(Objects::nonNull).distinct().collect(Collectors.joining(", ")));
            m.put("reference", s.getVerificationNo());
            m.put("sleeperIds", items.stream().map(SleeperScrapVerificationItem::getSleeperNo).toList());
            m.put("sleepers", items.stream().map(this::itemMap).toList());
            m.put("remarks", s.getRemarks());
            m.put("rejectedQty", null);
            m.put("scrappedQty", (long) items.size());
            String role = ieRoleForScrap(s.getShift(), s.getIeType());
            m.put("ieName", engineerLabel(s.getIeName(), role));
            m.put("engineer", s.getIeName());
            m.put("ieRole", role);
            m.put("scrapVerificationId", s.getVerificationNo());
            m.put("verificationId", s.getId());
            m.put("imageUrls", imagesById.getOrDefault(s.getId(), Collections.emptyList()));
            m.put("editable", isEditable(s, scope));
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

        String batchFilter = trimToNull(batchNo);
        String sleeperFilter = trimToNull(sleeperId);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> m : all) {
            LocalDate d = toDate(m.get("date"));
            if (from != null && (d == null || d.isBefore(from))) continue;
            if (to != null && (d == null || d.isAfter(to))) continue;
            if (txnType != null && !txnType.isBlank() && !txnType.equalsIgnoreCase((String) m.get("rowType"))) continue;
            if (batchFilter != null && !Objects.toString(m.get("batchNo"), "").toUpperCase().contains(batchFilter.toUpperCase())) continue;
            if (sleeperFilter != null) {
                @SuppressWarnings("unchecked")
                List<String> ids = (List<String>) m.get("sleeperIds");
                String q = sleeperFilter.toUpperCase();
                if (ids.stream().noneMatch(s -> s != null && s.toUpperCase().contains(q))) continue;
            }
            rows.add(m);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("plantId", pid);
        out.put("manufacturer", plant != null ? plant.getCompanyName() : null);
        out.put("placeOfInspection", placeName(plant, pid));
        out.put("totalRejected", totalRejected);
        out.put("totalScrapped", totalScrapped);
        out.put("pendingBalance", pending);
        out.put("pendingScrapQty", pending);
        out.put("canScrap", scope.canScrap(pid));
        out.put("ieType", scope.canScrap(pid) ? scope.ieTypeFor(pid) : null);
        LocalDate since = LocalDate.now().minusDays(RECENT_DAYS - 1L);
        out.put("productionLast30Days", productionSince(since, pid).getOrDefault(pid, 0L));
        out.put("rejectedLast30Days", rejections.stream().filter(e -> e.date != null && !e.date.isBefore(since)).count());
        out.put("declarationText", DECLARATION_TEXT);
        out.put("unidentifiedDefects", loaded.unidentified(pid));
        out.put("pendingSleepers", pendingRows(pendingMap)); // basket detail grid, saves a /pending call
        out.put("rows", rows); // chronological, like a passbook
        return out;
    }

    // =====================================================================
    // Scrap verification: view / add / edit / delete
    // =====================================================================

    public Map<String, Object> getVerification(Object principal, Long id) {
        Scope scope = resolveScope(principal);
        SleeperScrapVerification s = scrapRepo.findById(id).orElseThrow(() -> error("Scrap verification not found."));
        assertCanSee(scope, s.getPlantId());
        return toDetail(s, scope);
    }

    @Transactional
    public Map<String, Object> addVerification(Object principal, SleeperScrapVerificationRequest req) {
        Scope scope = resolveScope(principal);
        String pid = requirePlant(req.getPlantId());
        if (scope.userId == null || !scope.canScrap(pid)) {
            throw error("Only an IE mapped to this plant can record a scrap verification.");
        }
        if (!Boolean.TRUE.equals(req.getDeclarationAccepted())) {
            throw error("Please accept the declaration that the selected sleepers have been permanently damaged.");
        }
        requirePhotos(req.getBeforePhotos(), "before scrapping");
        requirePhotos(req.getAfterPhotos(), "after scrapping");
        LocalDate date = req.getScrappingDate() != null ? req.getScrappingDate() : LocalDate.now();
        if (date.isAfter(LocalDate.now())) throw error("Scrapping date cannot be in the future.");

        // Serialise per plant so two submissions can't scrap the same sleeper
        scrapRepo.lockPlant(pid);
        Map<String, Rejection> pending = pendingRejections(pid);
        List<Rejection> selected = resolveSelection(pid, req.getSleepers(), pending, Map.of());

        VendorPlant plant = plantFor(pid);
        SleeperScrapVerification s = new SleeperScrapVerification();
        s.setVerificationNo(nextVerificationNo());
        s.setPlantId(pid);
        s.setVendorCode(plant != null ? plant.getVendorCode() : null);
        s.setCompanyName(plant != null ? plant.getCompanyName() : null);
        s.setPlantName(plant != null ? plant.getPlantName() : null);
        s.setScrappingDate(date);
        s.setShift(trimToNull(req.getShift()));
        s.setAvailablePendingQty(pending.size());
        s.setQtyScrapped(selected.size());
        s.setDeclarationAccepted(true);
        s.setDeclarationText(DECLARATION_TEXT);
        s.setRemarks(trimToNull(req.getRemarks()));
        s.setIeUserId(scope.userId);
        s.setIeName(scope.userName);
        s.setIeType(scope.ieTypeFor(pid));
        s.setStatus(SleeperScrapVerification.STATUS_ACTIVE);
        s.setCreatedBy(String.valueOf(scope.userId));
        s = scrapRepo.save(s);

        insertItems(s.getId(), selected);
        savePhotos(s, req.getBeforePhotos(), SleeperScrapVerificationImage.BEFORE);
        savePhotos(s, req.getAfterPhotos(), SleeperScrapVerificationImage.AFTER);
        log.info("Sleeper scrap verification {} recorded: plant {}, {} sleepers by user {}",
                s.getVerificationNo(), pid, selected.size(), scope.userId);
        return toDetail(s, scope);
    }

    @Transactional
    public Map<String, Object> updateVerification(Object principal, Long id, SleeperScrapVerificationRequest req) {
        Scope scope = resolveScope(principal);
        SleeperScrapVerification s = getEditableOrThrow(scope, id);
        String reason = requireReason(req.getReason());
        String actor = String.valueOf(scope.userId);

        if (req.getSleepers() != null) {
            scrapRepo.lockPlant(s.getPlantId());
            List<SleeperScrapVerificationItem> current = itemRepo.findByScrapVerificationIdAndActiveKeyIsNotNullOrderByIdAsc(s.getId());
            Map<String, SleeperScrapVerificationItem> currentByKey = new LinkedHashMap<>();
            for (SleeperScrapVerificationItem it : current) currentByKey.put(it.getActiveKey(), it);
            Map<String, Rejection> pending = pendingRejections(s.getPlantId());
            List<Rejection> selected = resolveSelection(s.getPlantId(), req.getSleepers(), pending, currentByKey);
            Set<String> selectedKeys = selected.stream().map(Rejection::plantKey).collect(Collectors.toSet());

            String before = current.stream().map(this::label).collect(Collectors.joining(", "));
            // Removed sleepers go back to the pending basket, added ones are inserted (one statement each way)
            List<Long> released = current.stream().filter(it -> !selectedKeys.contains(it.getActiveKey()))
                    .map(SleeperScrapVerificationItem::getId).toList();
            if (!released.isEmpty()) itemRepo.releaseByIds(released);
            insertItems(s.getId(), selected.stream().filter(e -> !currentByKey.containsKey(e.plantKey())).toList());
            String after = selected.stream().map(e -> e.sleeperNo + " (Batch " + Objects.toString(e.batchNo, "-") + ")")
                    .collect(Collectors.joining(", "));
            if (!before.equals(after) || current.size() != selected.size()) {
                audit(s.getId(), "EDIT", "sleepers", before, after, reason, actor);
                s.setQtyScrapped(selected.size());
            }
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
        replacePhotos(s, req.getBeforePhotos(), SleeperScrapVerificationImage.BEFORE, reason, actor);
        replacePhotos(s, req.getAfterPhotos(), SleeperScrapVerificationImage.AFTER, reason, actor);

        s.setUpdatedBy(actor);
        s = scrapRepo.save(s);
        return toDetail(s, scope);
    }

    @Transactional
    public void deleteVerification(Object principal, Long id, String reason) {
        Scope scope = resolveScope(principal);
        SleeperScrapVerification s = getEditableOrThrow(scope, id);
        String why = requireReason(reason);
        String actor = String.valueOf(scope.userId);
        List<SleeperScrapVerificationItem> items = itemRepo.findByScrapVerificationIdAndActiveKeyIsNotNullOrderByIdAsc(s.getId());
        audit(s.getId(), "DELETE", "status", s.getStatus(), SleeperScrapVerification.STATUS_DELETED, why, actor);
        audit(s.getId(), "DELETE", "sleepers", items.stream().map(this::label).collect(Collectors.joining(", ")),
                "returned to pending basket", why, actor);
        itemRepo.releaseAll(s.getId()); // sleepers back to the pending basket, one statement
        s.setStatus(SleeperScrapVerification.STATUS_DELETED);
        s.setUpdatedBy(actor);
        scrapRepo.save(s);
        log.info("Sleeper scrap verification {} deleted by user {}: {}", s.getVerificationNo(), scope.userId, why);
    }

    /**
     * Turns the selected sleeper references into rejections that are pending (or already in this entry, on edit).
     * Every sleeper must exist in the pending basket.
     */
    private List<Rejection> resolveSelection(String plantId, List<SleeperScrapVerificationRequest.SleeperRef> refs,
                                             Map<String, Rejection> pending,
                                             Map<String, SleeperScrapVerificationItem> alreadyInEntry) {
        if (refs == null || refs.isEmpty()) throw error("Please select at least one sleeper to scrap.");
        if (refs.size() > MAX_SLEEPERS_PER_ENTRY) {
            throw error("A maximum of " + MAX_SLEEPERS_PER_ENTRY + " sleepers can be scrapped in one entry.");
        }
        Map<String, Rejection> selected = new LinkedHashMap<>();
        for (SleeperScrapVerificationRequest.SleeperRef ref : refs) {
            if (ref == null || trimToNull(ref.getSleeperNo()) == null) continue;
            String plantKey = plantId + "|" + sleeperKey(ref.getBatchNo(), ref.getSleeperNo());
            if (selected.containsKey(plantKey)) continue;
            Rejection e = pending.get(plantKey);
            if (e == null && alreadyInEntry.containsKey(plantKey)) {
                e = fromItem(alreadyInEntry.get(plantKey));
            }
            if (e == null) {
                throw error("Sleeper " + ref.getSleeperNo().trim() + " (Batch " + Objects.toString(trimToNull(ref.getBatchNo()), "-")
                        + ") is not pending in the rejection basket: it was not rejected or has already been scrapped.");
            }
            selected.put(plantKey, e);
        }
        if (selected.isEmpty()) throw error("Please select at least one sleeper to scrap.");
        return new ArrayList<>(selected.values());
    }

    /**
     * Inserts the scrapped sleepers with one multi-row INSERT per INSERT_CHUNK rows (IDENTITY ids rule out
     * Hibernate batching, and the driver does not rewrite JDBC batches). The UNIQUE active_key turns a
     * concurrent double scrap into a clear error.
     */
    private void insertItems(Long verificationId, List<Rejection> sleepers) {
        if (sleepers.isEmpty()) return;
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        for (int from = 0; from < sleepers.size(); from += INSERT_CHUNK) {
            List<Rejection> chunk = sleepers.subList(from, Math.min(from + INSERT_CHUNK, sleepers.size()));
            StringBuilder sql = new StringBuilder("INSERT INTO sleeper_scrap_verification_item (scrap_verification_id, plant_id, "
                    + "batch_no, sleeper_no, sleeper_key, active_key, sleeper_type, stage, rejection_reason, rejection_date, created_date) VALUES ");
            List<Object> args = new ArrayList<>(chunk.size() * 11);
            for (int i = 0; i < chunk.size(); i++) {
                Rejection e = chunk.get(i);
                sql.append(i == 0 ? "" : ",").append("(?,?,?,?,?,?,?,?,?,?,?)");
                args.add(verificationId);
                args.add(e.plantId);
                args.add(e.batchNo);
                args.add(e.sleeperNo);
                args.add(e.key);
                args.add(e.plantKey());
                args.add(e.sleeperType);
                args.add(e.stage);
                args.add(e.reason != null && e.reason.length() > 500 ? e.reason.substring(0, 500) : e.reason);
                args.add(e.date != null ? java.sql.Date.valueOf(e.date) : null);
                args.add(now);
            }
            try {
                jdbcTemplate.update(sql.toString(), args.toArray());
            } catch (DuplicateKeyException ex) {
                throw error("One or more selected sleepers were scrapped in another entry just now. Please refresh and try again.");
            }
        }
    }

    private Rejection fromItem(SleeperScrapVerificationItem it) {
        Rejection e = new Rejection();
        e.plantId = it.getPlantId();
        e.batchNo = it.getBatchNo();
        e.sleeperNo = it.getSleeperNo();
        e.key = it.getSleeperKey();
        e.sleeperType = it.getSleeperType();
        e.stage = it.getStage();
        e.reason = it.getRejectionReason();
        e.date = it.getRejectionDate();
        return e;
    }

    private Map<String, Object> itemMap(SleeperScrapVerificationItem it) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("batchNo", it.getBatchNo());
        m.put("sleeperNo", it.getSleeperNo());
        m.put("sleeperType", it.getSleeperType());
        m.put("stage", it.getStage());
        m.put("stageLabel", stageLabel(it.getStage()));
        m.put("reason", it.getRejectionReason());
        m.put("rejectionDate", it.getRejectionDate() != null ? it.getRejectionDate().toString() : null);
        return m;
    }

    private String label(SleeperScrapVerificationItem it) {
        return it.getSleeperNo() + " (Batch " + Objects.toString(it.getBatchNo(), "-") + ")";
    }

    private SleeperScrapVerification getEditableOrThrow(Scope scope, Long id) {
        SleeperScrapVerification s = scrapRepo.findById(id).orElseThrow(() -> error("Scrap verification not found."));
        if (SleeperScrapVerification.STATUS_DELETED.equals(s.getStatus())) {
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

    private boolean isEditable(SleeperScrapVerification s, Scope scope) {
        return SleeperScrapVerification.STATUS_ACTIVE.equals(s.getStatus())
                && scope.userId != null && scope.userId.equals(s.getIeUserId())
                && s.getCreatedDate() != null
                && ChronoUnit.MINUTES.between(s.getCreatedDate(), LocalDateTime.now()) <= EDIT_WINDOW_MINUTES;
    }

    private Map<String, Object> toDetail(SleeperScrapVerification s, Scope scope) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("verificationNo", s.getVerificationNo());
        m.put("scrapVerificationId", s.getVerificationNo());
        m.put("plantId", s.getPlantId());
        m.put("vendorCode", s.getVendorCode());
        m.put("manufacturer", s.getCompanyName());
        m.put("placeOfInspection", s.getPlantName());
        m.put("scrappingDate", s.getScrappingDate() != null ? s.getScrappingDate().toString() : null);
        m.put("shift", s.getShift());
        m.put("availablePendingQty", s.getAvailablePendingQty());
        m.put("qtyScrapped", s.getQtyScrapped());
        m.put("declarationAccepted", s.getDeclarationAccepted());
        m.put("declarationText", s.getDeclarationText());
        m.put("remarks", s.getRemarks());
        m.put("engineer", engineerLabel(s.getIeName(), ieRoleForScrap(s.getShift(), s.getIeType())));
        m.put("ieType", s.getIeType());
        m.put("status", s.getStatus());
        m.put("createdAt", withZone(s.getCreatedDate()));
        m.put("lockedAt", s.getCreatedDate() != null ? withZone(s.getCreatedDate().plusMinutes(EDIT_WINDOW_MINUTES)) : null);
        m.put("editable", isEditable(s, scope));
        m.put("sleepers", itemRepo.findByScrapVerificationIdAndActiveKeyIsNotNullOrderByIdAsc(s.getId())
                .stream().map(this::itemMap).toList());

        List<String> before = new ArrayList<>(), after = new ArrayList<>();
        for (SleeperScrapVerificationImage img : imageRepo.findByScrapVerificationIdOrderByIdAsc(s.getId())) {
            (SleeperScrapVerificationImage.BEFORE.equals(img.getPhotoType()) ? before : after).add(img.getImageUrl());
        }
        m.put("beforePhotos", before);
        m.put("afterPhotos", after);

        List<Map<String, Object>> audit = new ArrayList<>();
        for (SleeperScrapVerificationAudit a : auditRepo.findByScrapVerificationIdOrderByActionAtDesc(s.getId())) {
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

    private void requirePhotos(List<SleeperScrapVerificationRequest.Photo> photos, String label) {
        long count = photos == null ? 0 : photos.stream().filter(p -> p != null && p.getBase64Data() != null && !p.getBase64Data().isBlank()).count();
        if (count == 0) throw error("Please add at least one photo " + label + ".");
        if (count > MAX_PHOTOS_PER_TYPE) throw error("A maximum of " + MAX_PHOTOS_PER_TYPE + " photos " + label + " is allowed.");
    }

    private void savePhotos(SleeperScrapVerification s, List<SleeperScrapVerificationRequest.Photo> photos, String type) {
        int n = 0;
        for (SleeperScrapVerificationRequest.Photo p : photos) {
            if (p == null || p.getBase64Data() == null || p.getBase64Data().isBlank()) continue;
            n++;
            String blobName = "sleeper/" + s.getVerificationNo() + "/" + type + "_" + n + "_" + System.currentTimeMillis() + ".jpg";
            String url = azureBlobStorageService.uploadBase64File(p.getBase64Data(), blobName, IMAGE_CONTAINER, TARGET_IMAGE_KB);
            if (url == null || url.isBlank()) throw error("Could not upload the " + type.toLowerCase() + " photo. Please try again.");
            SleeperScrapVerificationImage img = new SleeperScrapVerificationImage();
            img.setScrapVerificationId(s.getId());
            img.setPhotoType(type);
            img.setImageUrl(url);
            img.setBlobName(blobName);
            img.setCreatedBy(s.getCreatedBy());
            imageRepo.save(img);
        }
    }

    private void replacePhotos(SleeperScrapVerification s, List<SleeperScrapVerificationRequest.Photo> photos, String type,
                               String reason, String actor) {
        if (photos == null || photos.isEmpty()) return;
        requirePhotos(photos, type.equals(SleeperScrapVerificationImage.BEFORE) ? "before scrapping" : "after scrapping");
        List<SleeperScrapVerificationImage> old = imageRepo.findByScrapVerificationIdAndPhotoType(s.getId(), type);
        audit(s.getId(), "EDIT", type.toLowerCase() + "_photos",
                String.join(", ", old.stream().map(SleeperScrapVerificationImage::getBlobName).toList()),
                photos.size() + " new photo(s)", reason, actor);
        imageRepo.deleteAll(old); // old blobs stay in storage as part of the audit trail
        savePhotos(s, photos, type);
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private void audit(Long id, String action, String field, String oldVal, String newVal, String reason, String actor) {
        SleeperScrapVerificationAudit a = new SleeperScrapVerificationAudit();
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
        String prefix = "SSV-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + "-";
        Long max = scrapRepo.maxSequenceForPrefix(prefix + "%");
        return prefix + String.format("%02d", (max != null ? max : 0) + 1);
    }

    /** vendor_plant rows of the given plants only (plant ids are stored with and without a leading ':') */
    private Map<String, VendorPlant> plantsFor(Collection<String> plantIds) {
        Map<String, VendorPlant> map = new HashMap<>();
        if (plantIds == null || plantIds.isEmpty()) return map;
        List<String> variants = new ArrayList<>(plantIds.size() * 2);
        for (String pid : plantIds) {
            if (pid == null) continue;
            variants.add(pid);
            variants.add(":" + pid);
        }
        for (VendorPlant p : timed("vendorPlants", () -> vendorPlantRepo.findByPlantIdIn(variants))) {
            String pid = normPlant(p.getPlantId());
            if (pid != null) map.putIfAbsent(pid, p);
        }
        return map;
    }

    /** Sleepers cast per plant since the given date, from the vendors' production declarations (one query) */
    private Map<String, Long> productionSince(LocalDate since, String plantId) {
        Map<String, Long> map = new HashMap<>();
        for (Object[] r : timed("production", () -> scrapRepo.sumProductionSince(since, plantId == null ? "" : plantId))) {
            String pid = normPlant(str(r[0]));
            if (pid != null && r[1] instanceof Number n) map.merge(pid, n.longValue(), Long::sum);
        }
        return map;
    }

    private VendorPlant plantFor(String plantId) {
        return plantsFor(List.of(plantId)).get(plantId);
    }

    private static String placeName(VendorPlant plant, String pid) {
        return plant != null && plant.getPlantName() != null ? plant.getPlantName() : pid;
    }

    /** "Shift A", "a", "GENERAL", "G" -> "A" / "GENERAL"; null when unknown */
    static String normShift(String shift) {
        if (shift == null) return null;
        String s = shift.trim().toUpperCase().replaceFirst("^SHIFT[\\s-]*", "");
        if (s.equals("GENERAL") || s.equals("G") || s.equals("GEN")) return "GENERAL";
        return s.matches("[A-C]") ? s : null;
    }

    /**
     * Inspecting engineer (SRS): demoulding is logged by the Shift IE of that shift; final inspection and
     * destructive tests by the General IE.
     */
    static String ieRoleForRejection(String stage, String shift) {
        if (!STAGE_DEMOULDING.equals(stage)) return "IE-General";
        String s = normShift(shift);
        if ("GENERAL".equals(s)) return "IE-General";
        return s != null ? "IE-Shift " + s : null;
    }

    /** Engineer of a scrap verification: by the duty shift recorded on it, else by IE type */
    static String ieRoleForScrap(String shift, String ieType) {
        String s = normShift(shift);
        if ("GENERAL".equals(s)) return "IE-General";
        if (s != null) return "IE-Shift " + s;
        if ("MAIN_IE".equals(ieType)) return "Main IE";
        if ("PROCESS_IE".equals(ieType)) return "Process IE";
        return null;
    }

    /** "Nitin (IE-Shift A)"; just the name or just the role when the other is unknown */
    static String engineerLabel(String name, String role) {
        if (name == null || name.isBlank()) return role;
        return role == null ? name : name + " (" + role + ")";
    }

    /** Shift for display and grouping: "Shift A" / "a" -> "A", "General" -> "General"; other text kept as is */
    static String shiftLabel(String shift) {
        String s = normShift(shift);
        if ("GENERAL".equals(s)) return "General";
        if (s != null) return s;
        return shift == null || shift.isBlank() ? null : shift.trim();
    }

    /**
     * Reason-wise count of a group of rejected sleepers, most frequent first. A demoulding sleeper with both a visual
     * and a dimensional reason ("Crack; Honeycomb") counts under each reason.
     */
    static List<Map<String, Object>> reasonBreakup(List<Rejection> group) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        Map<String, String> display = new HashMap<>();
        for (Rejection e : group) {
            String reasons = e.reason == null || e.reason.isBlank() ? "Not specified" : e.reason;
            for (String part : reasons.split("\\s*;\\s*")) {
                if (part.isBlank()) continue;
                String k = part.trim().toUpperCase();
                display.putIfAbsent(k, part.trim());
                counts.merge(k, 1, Integer::sum);
            }
        }
        List<Map<String, Object>> out = new ArrayList<>();
        counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(en -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("reason", display.get(en.getKey()));
                    m.put("count", en.getValue());
                    out.add(m);
                });
        return out;
    }

    static String stageLabel(String stage) {
        if (STAGE_DEMOULDING.equals(stage)) return "Demoulding";
        if (STAGE_TESTING.equals(stage)) return "MR Test";
        if (STAGE_FINAL.equals(stage)) return "Final Insp.";
        return stage;
    }

    /**
     * Normalised batch|sleeper identity, so the same sleeper matches whether it was written as
     * "210-A", "210A" or "601/210A" (batch prefix) at different stages.
     */
    static String sleeperKey(String batchNo, String sleeperNo) {
        String b = batchNo == null ? "" : batchNo.trim().toUpperCase().replaceAll("\\s+", "");
        String s = sleeperNo == null ? "" : sleeperNo.trim().toUpperCase();
        if (!b.isEmpty()) {
            for (String sep : new String[]{"/", "-", "_"}) {
                if (s.startsWith(b + sep)) {
                    s = s.substring(b.length() + 1);
                    break;
                }
            }
        }
        return b + "|" + s.replaceAll("[^A-Z0-9]", "");
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

    private static InvalidInputException error(String message) {
        return new InvalidInputException(new ErrorDetails(400, 400, "error", message));
    }

    /** Plant ids / vendor codes are stored with and without a leading ':' */
    static String normPlant(String v) {
        if (v == null) return null;
        String s = v.trim().replaceAll("^:+", "");
        return s.isEmpty() ? null : s;
    }

    private static String trimToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static String str(Object o) {
        if (o == null) return null;
        String s = o.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private static String nameOr(String name, Object userId) {
        return name != null ? name : (userId != null && !userId.toString().isBlank() ? "User " + userId : null);
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
