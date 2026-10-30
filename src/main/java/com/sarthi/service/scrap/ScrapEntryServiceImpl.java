package com.sarthi.service.scrap;

import com.sarthi.dto.scrap.*;
import com.sarthi.entity.scrap.IeScrapEntry;
import com.sarthi.entity.scrap.IeScrapEntryAudit;
import com.sarthi.entity.scrap.IeScrapEntryImage;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.exception.InvalidInputException;
import com.sarthi.repository.IePincodePoiMappingRepository;
import com.sarthi.repository.PoiProcessIeMappingRepository;
import com.sarthi.repository.UserMasterRepository;
import com.sarthi.repository.finalmaterial.*;
import com.sarthi.repository.processmaterial.*;
import com.sarthi.repository.scrap.IeScrapEntryAuditRepository;
import com.sarthi.repository.scrap.IeScrapEntryImageRepository;
import com.sarthi.repository.scrap.IeScrapEntryRepository;
import com.sarthi.service.AzureBlobStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScrapEntryServiceImpl implements ScrapEntryService {

    private static final String CONTAINER_SCRAP_IMAGES = "scrap-images";
    private static final String BLOB_FOLDER_ERC = "erc/";
    private static final int TARGET_KB_SCRAP_IMAGE = 1400; // ~1.4 MB — high quality for railway board display
    private static final int EDIT_WINDOW_MINUTES = 60;
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_DELETED = "DELETED";

    @Autowired private IeScrapEntryRepository scrapEntryRepo;
    @Autowired private IeScrapEntryImageRepository imageRepo;
    @Autowired private IeScrapEntryAuditRepository auditRepo;
    @Autowired private IePincodePoiMappingRepository iePincodePoiMappingRepository;
    @Autowired private PoiProcessIeMappingRepository poiProcessIeMappingRepository;
    @Autowired private UserMasterRepository userMasterRepository;
    @Autowired private AzureBlobStorageService azureBlobStorageService;
    @Autowired private ProcessLineFinalResultRepository processLineFinalResultRepository;
    @Autowired private FinalCumulativeResultsRepository finalCumulativeResultsRepository;

    // Process stage repositories for rejection breakup
    @Autowired private ProcessShearingDataRepository processShearingDataRepository;
    @Autowired private ProcessTurningDataRepository processTurningDataRepository;
    @Autowired private ProcessMpiDataRepository processMpiDataRepository;
    @Autowired private ProcessForgingDataRepository processForgingDataRepository;
    @Autowired private ProcessQuenchingDataRepository processQuenchingDataRepository;
    @Autowired private ProcessTemperingDataRepository processTemperingDataRepository;
    @Autowired private ProcessFinalCheckDataRepository processFinalCheckDataRepository;
    @Autowired private ProcessTestingFinishingDataRepository processTestingFinishingDataRepository;

    // Final test repositories for rejection breakup
    @Autowired private FinalVisualInspectionRepository finalVisualInspectionRepository;
    @Autowired private FinalDimensionalInspectionRepository finalDimensionalInspectionRepository;
    @Autowired private FinalHardnessTestRepository finalHardnessTestRepository;
    @Autowired private FinalFreedomFromDefectsTestRepository finalFreedomFromDefectsTestRepository;
    @Autowired private FinalToeLoadTestRepository finalToeLoadTestRepository;
    @Autowired private FinalWeightTestRepository finalWeightTestRepository;

    @Override
    public List<PlantInfoDto> getMappedPlants(Integer ieUserId, String ieType) {
        var user = userMasterRepository.findById(ieUserId).orElse(null);
        if (user == null) return Collections.emptyList();
        String empCode = user.getEmployeeCode();

        List<Object[]> rows;
        if ("PROCESS_IE".equalsIgnoreCase(ieType)) {
            rows = poiProcessIeMappingRepository.findPlantInfoByEmployeeCode(empCode);
        } else {
            rows = iePincodePoiMappingRepository.findPlantInfoByEmployeeCode(empCode);
        }

        return rows.stream().map(r -> new PlantInfoDto(
                r[0] != null ? r[0].toString() : null,
                r[1] != null ? r[1].toString() : null,
                r[2] != null ? r[2].toString() : null
        )).collect(Collectors.toList());
    }

    @Override
    public ScrapLedgerResponseDto getLedger(String poiCode, String unitLineShedNo) {
        boolean hasLineFilter = unitLineShedNo != null && !unitLineShedNo.isBlank();
        List<ScrapLedgerRowDto> allRows = new ArrayList<>();

        // ── PROCESS rejection rows ─────────────────────────────────────────────
        // columns: [0]=entryDate, [1]=shift, [2]=unitLineShedNo, [3]=rejectedQty, [4]=ieName
        List<Object[]> processRows = processLineFinalResultRepository.findProcessRejectionRowsByPoiCode(poiCode);
        for (Object[] r : processRows) {
            ScrapLedgerRowDto row = new ScrapLedgerRowDto();
            row.setRowType("REJECTION");
            row.setStageOfInspection("PROCESS");
            row.setEntryDate(r[0] != null ? LocalDate.parse(r[0].toString()) : null);
            row.setShift(r[1] != null ? r[1].toString() : null);
            row.setUnitLineShedNo(r[2] != null ? r[2].toString() : null);
            row.setRejectedQty(r[3] != null ? ((Number) r[3]).intValue() : 0);
            row.setScrappedQty(0);
            row.setIeName(r[4] != null ? r[4].toString() : null);
            row.setEditable(false);
            row.setImageUrls(Collections.emptyList());
            allRows.add(row);
        }

        // ── FINAL rejection rows ───────────────────────────────────────────────
        List<Object[]> finalRows = finalCumulativeResultsRepository.findFinalRejectionRowsByPoiCode(poiCode);
        for (Object[] r : finalRows) {
            ScrapLedgerRowDto row = new ScrapLedgerRowDto();
            row.setRowType("REJECTION");
            row.setStageOfInspection("FINAL");
            row.setEntryDate(r[0] != null ? LocalDate.parse(r[0].toString()) : null);
            row.setShift(null);
            row.setUnitLineShedNo(null);
            row.setRejectedQty(r[1] != null ? ((Number) r[1]).intValue() : 0);
            row.setScrappedQty(0);
            row.setIeName(r[2] != null ? r[2].toString() : null);
            row.setEditable(false);
            row.setImageUrls(Collections.emptyList());
            allRows.add(row);
        }

        // ── SCRAP rows ─────────────────────────────────────────────────────────
        List<IeScrapEntry> scrapEntries = hasLineFilter
                ? scrapEntryRepo.findByPoiCodeAndUnitLineShedNoAndStatusOrderByCreatedDateDesc(poiCode, unitLineShedNo, STATUS_ACTIVE)
                : scrapEntryRepo.findByPoiCodeAndStatusOrderByCreatedDateDesc(poiCode, STATUS_ACTIVE);

        List<Integer> entryIds = scrapEntries.stream().map(IeScrapEntry::getId).collect(Collectors.toList());
        Map<Integer, List<String>> imagesByEntryId = Collections.emptyMap();
        if (!entryIds.isEmpty()) {
            imagesByEntryId = imageRepo.findByScrapEntryIdIn(entryIds).stream()
                    .collect(Collectors.groupingBy(
                            IeScrapEntryImage::getScrapEntryId,
                            Collectors.mapping(IeScrapEntryImage::getImageUrl, Collectors.toList())
                    ));
        }

        // Batch-resolve scrap entry creator IDs → full names (1 query, no N+1)
        List<Integer> creatorIds = scrapEntries.stream()
                .map(e -> { try { return Integer.parseInt(e.getCreatedBy()); } catch (Exception ex) { return null; } })
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, String> userNameMap = userMasterRepository.findByUserIdIn(creatorIds).stream()
                .collect(Collectors.toMap(
                        u -> u.getUserId(),
                        u -> u.getFullName() != null ? u.getFullName()
                             : u.getShortName() != null ? u.getShortName()
                             : u.getEmployeeCode() != null ? u.getEmployeeCode() : ""));

        LocalDateTime now = LocalDateTime.now();
        int totalScrapped = 0;
        for (IeScrapEntry entry : scrapEntries) {
            totalScrapped += entry.getNoOfPiecesScrapped();
            ScrapLedgerRowDto row = new ScrapLedgerRowDto();
            row.setRowType("SCRAP");
            row.setEntryId(entry.getId());
            row.setEntryDate(entry.getScrappingDate());
            row.setShift(entry.getShift());
            row.setUnitLineShedNo(entry.getUnitLineShedNo());
            row.setStageOfInspection(entry.getIeType());
            row.setRejectedQty(0);
            row.setScrappedQty(entry.getNoOfPiecesScrapped());
            Integer creatorId = null;
            try { creatorId = Integer.parseInt(entry.getCreatedBy()); } catch (Exception ignored) {}
            row.setIeName(creatorId != null ? userNameMap.getOrDefault(creatorId, entry.getCreatedBy()) : entry.getCreatedBy());
            row.setRemarks(entry.getRemarks());
            row.setScrapVerificationId(entry.getScrapVerificationId());
            row.setCreatedDate(entry.getCreatedDate());
            row.setEditable(ChronoUnit.MINUTES.between(entry.getCreatedDate(), now) <= EDIT_WINDOW_MINUTES);
            row.setImageUrls(imagesByEntryId.getOrDefault(entry.getId(), Collections.emptyList()));
            allRows.add(row);
        }

        // ── Sort chronologically ASC, compute running balance, then reverse ────
        allRows.sort(Comparator.comparing(ScrapLedgerRowDto::getEntryDate,
                Comparator.nullsLast(Comparator.naturalOrder())));

        int balance = 0;
        int totalRejected = 0;
        for (ScrapLedgerRowDto row : allRows) {
            if ("REJECTION".equals(row.getRowType())) {
                balance += row.getRejectedQty();
                totalRejected += row.getRejectedQty();
            } else {
                balance -= row.getScrappedQty();
            }
            row.setPendingBalance(balance);
        }
        Collections.reverse(allRows);

        ScrapLedgerResponseDto response = new ScrapLedgerResponseDto();
        response.setPoiCode(poiCode);
        response.setUnitLineShedNo(unitLineShedNo);
        response.setTotalRejected(totalRejected);
        response.setTotalScrapped(totalScrapped);
        response.setPendingBalance(Math.max(0, totalRejected - totalScrapped));
        response.setRows(allRows);
        return response;
    }

    @Override
    public Integer getPendingQty(String poiCode, LocalDate toDate) {
        long processRejected = Optional.ofNullable(
                processLineFinalResultRepository.sumRejectedByPoiUpToDate(poiCode, toDate)).orElse(0L);
        long finalRejected = Optional.ofNullable(
                finalCumulativeResultsRepository.sumRejectedByPoiUpToDate(poiCode, toDate)).orElse(0L);
        int totalScrapped = Optional.ofNullable(
                scrapEntryRepo.sumScrappedQtyByPoiUpToDate(poiCode, toDate)).orElse(0);
        return (int) Math.max(0, processRejected + finalRejected - totalScrapped);
    }

    @Override
    public List<BoardSummaryRowDto> getBoardSummary(String productType) {
        // Aggregate process rejections by POI
        Map<String, Long> processRejMap = new HashMap<>();
        for (Object[] r : processLineFinalResultRepository.sumProcessRejectedGroupByPoi()) {
            if (r[0] != null) processRejMap.put(r[0].toString(), ((Number) r[1]).longValue());
        }

        // Aggregate final rejections by POI
        Map<String, Long> finalRejMap = new HashMap<>();
        for (Object[] r : finalCumulativeResultsRepository.sumFinalRejectedGroupByPoi()) {
            if (r[0] != null) finalRejMap.put(r[0].toString(), ((Number) r[1]).longValue());
        }

        // Aggregate scrap by POI
        Map<String, Long> scrapMap = new HashMap<>();
        Map<String, LocalDate> lastScrapDateMap = new HashMap<>();
        for (Object[] r : scrapEntryRepo.sumScrappedGroupByPoi()) {
            if (r[0] != null) {
                scrapMap.put(r[0].toString(), ((Number) r[1]).longValue());
                if (r[2] != null) lastScrapDateMap.put(r[0].toString(), LocalDate.parse(r[2].toString()));
            }
        }

        // Gather all POI codes that appear in any of the three sources
        Set<String> allPoiCodes = new LinkedHashSet<>();
        allPoiCodes.addAll(processRejMap.keySet());
        allPoiCodes.addAll(finalRejMap.keySet());
        allPoiCodes.addAll(scrapMap.keySet());

        // Look up company/unit names from pincode_poi_mapping
        Map<String, String[]> poiInfoMap = new HashMap<>();
        List<Object[]> plantRows = iePincodePoiMappingRepository.findAllPlantInfoForPois(new ArrayList<>(allPoiCodes));
        for (Object[] r : plantRows) {
            if (r[0] != null) {
                poiInfoMap.put(r[0].toString(), new String[]{
                    r[1] != null ? r[1].toString() : null,
                    r[2] != null ? r[2].toString() : null
                });
            }
        }

        List<BoardSummaryRowDto> result = new ArrayList<>();
        for (String poi : allPoiCodes) {
            long totalRej = processRejMap.getOrDefault(poi, 0L) + finalRejMap.getOrDefault(poi, 0L);
            long totalScrap = scrapMap.getOrDefault(poi, 0L);
            String[] info = poiInfoMap.get(poi);
            result.add(new BoardSummaryRowDto(
                poi,
                info != null ? info[0] : null,
                info != null ? info[1] : null,
                totalRej,
                totalScrap,
                Math.max(0, totalRej - totalScrap),
                lastScrapDateMap.get(poi)
            ));
        }

        result.sort(Comparator.comparing(BoardSummaryRowDto::getPoiCode,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return result;
    }

    @Override
    @Transactional
    public ScrapEntryResponseDto addEntry(ScrapEntryRequestDto req) {
        IeScrapEntry entry = new IeScrapEntry();
        entry.setPoiCode(req.getPoiCode());
        entry.setUnitLineShedNo(req.getUnitLineShedNo());
        entry.setScrappingDate(req.getScrappingDate() != null ? req.getScrappingDate() : LocalDate.now());
        entry.setShift(req.getShift());
        entry.setNoOfPiecesScrapped(req.getNoOfPiecesScrapped());
        entry.setPendingQtyAtSubmit(getPendingQty(req.getPoiCode(),
                req.getScrappingDate() != null ? req.getScrappingDate() : LocalDate.now()));
        entry.setDeclarationAccepted(Boolean.TRUE.equals(req.getDeclarationAccepted()));
        entry.setRemarks(req.getRemarks());
        entry.setScrapVerificationId(generateVerificationId());
        entry.setIeUserId(req.getIeUserId());
        entry.setIeType(req.getIeType());
        entry.setStatus(STATUS_ACTIVE);
        entry.setCreatedBy(req.getActionBy());
        entry.setUpdatedBy(req.getActionBy());

        IeScrapEntry saved = scrapEntryRepo.save(entry);

        // Upload images to Azure — one call per image, no batch needed
        List<String> imageUrls = new ArrayList<>();
        if (req.getImages() != null) {
            for (ScrapEntryRequestDto.ScrapImageDto img : req.getImages()) {
                String blobName = BLOB_FOLDER_ERC + img.getFileName();
                String url = azureBlobStorageService.uploadBase64File(
                        img.getBase64Data(), blobName, CONTAINER_SCRAP_IMAGES, TARGET_KB_SCRAP_IMAGE);

                IeScrapEntryImage imageEntity = new IeScrapEntryImage();
                imageEntity.setScrapEntryId(saved.getId());
                imageEntity.setImageUrl(url);
                imageEntity.setBlobName(blobName);
                imageEntity.setCreatedBy(req.getActionBy());
                imageRepo.save(imageEntity);
                imageUrls.add(url);
            }
        }

        return toResponseDto(saved, imageUrls);
    }

    @Override
    @Transactional
    public ScrapEntryResponseDto updateEntry(Integer id, ScrapEntryUpdateDto req) {
        IeScrapEntry entry = getActiveEntryOrThrow(id);
        assertWithinEditWindow(entry);

        // Audit each changed field
        auditField(id, "scrapping_date", str(entry.getScrappingDate()), str(req.getScrappingDate()), req.getReason(), req.getActionBy());
        auditField(id, "shift", entry.getShift(), req.getShift(), req.getReason(), req.getActionBy());
        auditField(id, "no_of_pieces_scrapped", str(entry.getNoOfPiecesScrapped()), str(req.getNoOfPiecesScrapped()), req.getReason(), req.getActionBy());
        auditField(id, "remarks", entry.getRemarks(), req.getRemarks(), req.getReason(), req.getActionBy());

        if (req.getScrappingDate() != null)      entry.setScrappingDate(req.getScrappingDate());
        if (req.getShift() != null)               entry.setShift(req.getShift());
        if (req.getNoOfPiecesScrapped() != null)  entry.setNoOfPiecesScrapped(req.getNoOfPiecesScrapped());
        if (req.getRemarks() != null)             entry.setRemarks(req.getRemarks());
        entry.setUpdatedBy(req.getActionBy());

        // Replace images if provided
        List<String> imageUrls = new ArrayList<>();
        if (req.getImages() != null && !req.getImages().isEmpty()) {
            imageRepo.deleteByScrapEntryId(id);
            for (ScrapEntryRequestDto.ScrapImageDto img : req.getImages()) {
                String blobName = BLOB_FOLDER_ERC + img.getFileName();
                String url = azureBlobStorageService.uploadBase64File(
                        img.getBase64Data(), blobName, CONTAINER_SCRAP_IMAGES, TARGET_KB_SCRAP_IMAGE);
                IeScrapEntryImage imageEntity = new IeScrapEntryImage();
                imageEntity.setScrapEntryId(id);
                imageEntity.setImageUrl(url);
                imageEntity.setBlobName(blobName);
                imageEntity.setCreatedBy(req.getActionBy());
                imageRepo.save(imageEntity);
                imageUrls.add(url);
            }
        } else {
            imageUrls = imageRepo.findByScrapEntryId(id).stream()
                    .map(IeScrapEntryImage::getImageUrl).collect(Collectors.toList());
        }

        return toResponseDto(scrapEntryRepo.save(entry), imageUrls);
    }

    @Override
    @Transactional
    public void deleteEntry(Integer id, String reason, String actionBy) {
        IeScrapEntry entry = getActiveEntryOrThrow(id);
        assertWithinEditWindow(entry);

        entry.setStatus(STATUS_DELETED);
        entry.setUpdatedBy(actionBy);
        scrapEntryRepo.save(entry);

        IeScrapEntryAudit audit = new IeScrapEntryAudit();
        audit.setScrapEntryId(id);
        audit.setAction("DELETE");
        audit.setOldValue(STATUS_ACTIVE);
        audit.setNewValue(STATUS_DELETED);
        audit.setReason(reason);
        audit.setActionBy(actionBy);
        audit.setActionAt(LocalDateTime.now());
        auditRepo.save(audit);
    }

    @Override
    public RejectionBreakupDto getRejectionBreakup(String poiCode, LocalDate date, String stage, String shift, String lineName) {
        RejectionBreakupDto dto = new RejectionBreakupDto();
        dto.setDate(date);
        dto.setStage(stage);
        dto.setShift(shift);
        dto.setLineName(lineName);

        if ("PROCESS".equalsIgnoreCase(stage)) {
            // Use PLF for stage totals (authoritative) + stage tables filtered by callNo+lotNo for per-defect detail.
            // Filtering by lotNo avoids summing unrelated inspection calls that share the same date/shift/line.
            List<com.sarthi.entity.processmaterial.ProcessLineFinalResult> results =
                processLineFinalResultRepository.findResultsByPoiDateShiftLine(poiCode, date, shift, lineName);

            // Stage totals from PLF (sum across all PLF records for this date/shift/line)
            int shearTotal = 0, turnTotal = 0, mpiTotal = 0, forgTotal = 0;
            int quenchTotal = 0, tempTotal = 0, fcTotal = 0, tfTotal = 0;

            // Per-defect accumulators
            int cutLen = 0, ovality = 0, sharpEdges = 0, cracks = 0;
            int parLen = 0, fullTurn = 0, turnDia = 0;
            int mpiRej = 0;
            int forgTemp = 0, forgStab = 0, forgImproper = 0, forgDefect = 0, forgEmboss = 0;
            int qTemp = 0, qDur = 0, qHard = 0, qBox = 0, qFlat = 0, qFall = 0;
            int tTemp = 0, tDur = 0;
            int fcBox = 0, fcFlat = 0, fcFall = 0, fcSurf = 0, fcEmboss = 0, fcMark = 0, fcHard = 0;
            int tfToe = 0, tfWt = 0, tfPaint = 0, tfErc = 0;

            for (var r : results) {
                shearTotal  += c(r.getShearingRejected());
                turnTotal   += c(r.getTurningRejected());
                mpiTotal    += c(r.getMpiRejected());
                forgTotal   += c(r.getForgingRejected());
                quenchTotal += c(r.getQuenchingRejected());
                tempTotal   += c(r.getTemperingRejected());
                fcTotal     += c(r.getVisualCheckRejected()) + c(r.getDimensionsCheckRejected()) + c(r.getHardnessCheckRejected());
                tfTotal     += c(r.getTestingFinishingRejected());

                // For per-defect detail: match stage records saved within ±60 sec of this PLF entry
                // by the same createdBy user — same logic as getProcessDefectHourlyDetails (±3 min).
                // 60 sec covers slower stages (e.g. Tempering) that save after PLF in the same session.
                String callNo  = r.getInspectionCallNo();
                String rShift  = r.getShift();
                String rLine   = r.getLineNo();
                String rLot    = r.getLotNumber();
                String rUser   = r.getCreatedBy();
                LocalDateTime rTime = r.getCreatedAt() != null ? r.getCreatedAt() : LocalDateTime.now();
                LocalDateTime tFrom = rTime.minusSeconds(30);
                LocalDateTime tTo   = rTime.plusSeconds(30);

                for (var s : processShearingDataRepository.findByInspectionCallNo(callNo)) {
                    if (!inWindow(s.getShift(), s.getLineNo(), s.getLotNo(), s.getCreatedBy(), s.getCreatedAt(),
                                  rShift, rLine, rLot, rUser, tFrom, tTo)) continue;
                    cutLen     += c(s.getLengthCutBarRejected());
                    ovality    += c(s.getImproperDiaRejected());
                    sharpEdges += c(s.getSharpEdgesRejected());
                    cracks     += c(s.getCrackedEdgesRejected());
                }
                for (var t : processTurningDataRepository.findByInspectionCallNo(callNo)) {
                    if (!inWindow(t.getShift(), t.getLineNo(), t.getLotNo(), t.getCreatedBy(), t.getCreatedAt(),
                                  rShift, rLine, rLot, rUser, tFrom, tTo)) continue;
                    parLen   += c(t.getParallelLengthRejected());
                    fullTurn += c(t.getFullTurningLengthRejected());
                    turnDia  += c(t.getTurningDiaRejected());
                }
                for (var m : processMpiDataRepository.findByInspectionCallNo(callNo)) {
                    if (!inWindow(m.getShift(), m.getLineNo(), m.getLotNo(), m.getCreatedBy(), m.getCreatedAt(),
                                  rShift, rLine, rLot, rUser, tFrom, tTo)) continue;
                    mpiRej += c(m.getMpiRejected());
                }
                for (var f : processForgingDataRepository.findByInspectionCallNo(callNo)) {
                    if (!inWindow(f.getShift(), f.getLineNo(), f.getLotNo(), f.getCreatedBy(), f.getCreatedAt(),
                                  rShift, rLine, rLot, rUser, tFrom, tTo)) continue;
                    forgTemp    += c(f.getForgingTempRejected());
                    forgStab    += c(f.getForgingStabilisationRejectionRejected());
                    forgImproper += c(f.getImproperForgingRejected());
                    forgDefect  += c(f.getForgingDefectRejected());
                    forgEmboss  += c(f.getEmbossingDefectRejected());
                }
                for (var q : processQuenchingDataRepository.findByInspectionCallNo(callNo)) {
                    if (!inWindow(q.getShift(), q.getLineNo(), q.getLotNo(), q.getCreatedBy(), q.getCreatedAt(),
                                  rShift, rLine, rLot, rUser, tFrom, tTo)) continue;
                    qTemp += c(q.getQuenchingTemperatureRejected());
                    qDur  += c(q.getQuenchingDurationRejected());
                    qHard += c(q.getQuenchingHardnessRejected());
                    qBox  += c(q.getBoxGaugeRejected());
                    qFlat += c(q.getFlatBearingAreaRejected());
                    qFall += c(q.getFallingGaugeRejected());
                }
                for (var t : processTemperingDataRepository.findByInspectionCallNo(callNo)) {
                    if (!inWindow(t.getShift(), t.getLineNo(), t.getLotNo(), t.getCreatedBy(), t.getCreatedAt(),
                                  rShift, rLine, rLot, rUser, tFrom, tTo)) continue;
                    tTemp += c(t.getTemperingTemperatureRejected());
                    tDur  += c(t.getTemperingDurationRejected());
                }
                for (var fc : processFinalCheckDataRepository.findByInspectionCallNo(callNo)) {
                    if (!inWindow(fc.getShift(), fc.getLineNo(), fc.getLotNo(), fc.getCreatedBy(), fc.getCreatedAt(),
                                  rShift, rLine, rLot, rUser, tFrom, tTo)) continue;
                    fcBox   += c(fc.getBoxGaugeRejected());
                    fcFlat  += c(fc.getFlatBearingAreaRejected());
                    fcFall  += c(fc.getFallingGaugeRejected());
                    fcSurf  += c(fc.getSurfaceDefectRejected());
                    fcEmboss += c(fc.getEmbossingDefectRejected());
                    fcMark  += c(fc.getMarkingRejected());
                    fcHard  += c(fc.getTemperingHardnessRejected());
                }
                for (var tf : processTestingFinishingDataRepository.findByInspectionCallNo(callNo)) {
                    if (!inWindow(tf.getShift(), tf.getLineNo(), tf.getLotNo(), tf.getCreatedBy(), tf.getCreatedAt(),
                                  rShift, rLine, rLot, rUser, tFrom, tTo)) continue;
                    tfToe   += c(tf.getToeLoadRejected());
                    tfWt    += c(tf.getWeightRejected());
                    tfPaint += c(tf.getPaintIdentificationRejected());
                    tfErc   += c(tf.getErcCoatingRejected());
                }
            }

            List<RejectionBreakupDto.StageSection> sections = new ArrayList<>();
            if (shearTotal > 0)  sections.add(new RejectionBreakupDto.StageSection("Shearing", shearTotal,
                buildDefects(new String[]{"Cut Length", "Ovality", "Sharp Edges", "Cracks"}, new int[]{cutLen, ovality, sharpEdges, cracks})));
            if (turnTotal > 0)   sections.add(new RejectionBreakupDto.StageSection("Turning", turnTotal,
                buildDefects(new String[]{"Parallel Length", "Full Turning Length", "Turning Dia"}, new int[]{parLen, fullTurn, turnDia})));
            if (mpiTotal > 0)    sections.add(new RejectionBreakupDto.StageSection("MPI", mpiTotal,
                buildDefects(new String[]{"MPI Rejected"}, new int[]{mpiRej})));
            if (forgTotal > 0)   sections.add(new RejectionBreakupDto.StageSection("Forging", forgTotal,
                buildDefects(new String[]{"Forging Temp", "Stabilisation", "Improper Forging", "Forging Defect", "Embossing Defect"}, new int[]{forgTemp, forgStab, forgImproper, forgDefect, forgEmboss})));
            if (quenchTotal > 0) sections.add(new RejectionBreakupDto.StageSection("Quenching", quenchTotal,
                buildDefects(new String[]{"Temperature", "Duration", "Hardness", "Box Gauge", "Flat Bearing", "Falling Gauge"}, new int[]{qTemp, qDur, qHard, qBox, qFlat, qFall})));
            if (tempTotal > 0)   sections.add(new RejectionBreakupDto.StageSection("Tempering", tempTotal,
                buildDefects(new String[]{"Temperature", "Duration"}, new int[]{tTemp, tDur}, tempTotal)));
            if (fcTotal > 0)     sections.add(new RejectionBreakupDto.StageSection("Final Check", fcTotal,
                buildDefects(new String[]{"Box Gauge", "Flat Bearing", "Falling Gauge", "Surface Defect", "Embossing", "Marking", "Tempering Hardness"}, new int[]{fcBox, fcFlat, fcFall, fcSurf, fcEmboss, fcMark, fcHard})));
            if (tfTotal > 0)     sections.add(new RejectionBreakupDto.StageSection("Testing & Finishing", tfTotal,
                buildDefects(new String[]{"Toe Load", "Weight", "Paint Identification", "ERC Coating"}, new int[]{tfToe, tfWt, tfPaint, tfErc})));

            dto.setStageSections(sections);

        } else {
            // FINAL inspection
            List<String> callNos = finalCumulativeResultsRepository.findCallNosByPoiAndDate(poiCode, date);
            List<RejectionBreakupDto.StageSection> sections = new ArrayList<>();

            int visual = 0, dimensional = 0, hardness = 0, freedom = 0, toeLoad = 0, weight = 0;
            for (String callNo : callNos) {
                visual      += finalVisualInspectionRepository.findByInspectionCallNo(callNo).stream()
                    .mapToInt(v -> c(v.getTotalRejected())).sum();
                dimensional += finalDimensionalInspectionRepository.findByInspectionCallNo(callNo).stream()
                    .mapToInt(d -> c(d.getRejected())).sum();
                hardness    += finalHardnessTestRepository.findByInspectionCallNo(callNo).stream()
                    .mapToInt(h -> c(h.getRejected())).sum();
                freedom     += finalFreedomFromDefectsTestRepository.findByInspectionCallNo(callNo).stream()
                    .mapToInt(f -> c(f.getRejected())).sum();
                toeLoad     += finalToeLoadTestRepository.findByInspectionCallNo(callNo).stream()
                    .mapToInt(t -> c(t.getRejected())).sum();
                weight      += finalWeightTestRepository.findByInspectionCallNo(callNo).stream()
                    .mapToInt(w -> c(w.getRejected())).sum();
            }

            addSection(sections, "Visual Inspection",       buildDefects(new String[]{"Visual Rejected"},        new int[]{visual}));
            addSection(sections, "Dimensional Inspection",  buildDefects(new String[]{"Dimensional Rejected"},   new int[]{dimensional}));
            addSection(sections, "Hardness Test",           buildDefects(new String[]{"Hardness Rejected"},      new int[]{hardness}));
            addSection(sections, "Freedom from Defects",    buildDefects(new String[]{"Defects Rejected"},       new int[]{freedom}));
            addSection(sections, "Toe Load Test",           buildDefects(new String[]{"Toe Load Rejected"},      new int[]{toeLoad}));
            addSection(sections, "Weight Test",             buildDefects(new String[]{"Weight Rejected"},        new int[]{weight}));

            dto.setStageSections(sections);
        }

        return dto;
    }

    private boolean matchShiftLine(String eShift, String eLine, String shift, String lineName) {
        if (shift != null && !shift.equalsIgnoreCase(eShift)) return false;
        if (lineName != null && !lineName.equalsIgnoreCase(eLine)) return false;
        return true;
    }

    /**
     * Match a stage-table record to a specific PLF submission using:
     *   shift + lineNo + lotNo (soft) + createdBy + createdAt within ±5 seconds.
     * Same principle as getProcessDefectHourlyDetails but with a 5-second window
     * instead of 3 minutes, since sub-tables are saved in the same transaction.
     */
    private boolean inWindow(String eShift, String eLine, String eLot, String eUser, LocalDateTime eTime,
                              String shift, String lineNo, String lotNo, String user,
                              LocalDateTime from, LocalDateTime to) {
        if (shift != null && !shift.equalsIgnoreCase(eShift)) return false;
        if (lineNo != null && !lineNo.equalsIgnoreCase(eLine)) return false;
        if (lotNo != null && !lotNo.isBlank() && eLot != null && !lotNo.trim().equalsIgnoreCase(eLot.trim())) return false;
        if (user != null && !user.equals(eUser)) return false;
        if (eTime == null || eTime.isBefore(from) || eTime.isAfter(to)) return false;
        return true;
    }

    private int c(Integer val) { return val != null ? val : 0; }

    private List<RejectionBreakupDto.DefectEntry> buildDefects(String[] names, int[] values) {
        List<RejectionBreakupDto.DefectEntry> list = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            if (values[i] > 0) list.add(new RejectionBreakupDto.DefectEntry(names[i], values[i]));
        }
        return list;
    }

    /** Same as above, but falls back to a single "Rejected" entry using stageTotal when all sub-values are 0. */
    private List<RejectionBreakupDto.DefectEntry> buildDefects(String[] names, int[] values, int stageTotal) {
        List<RejectionBreakupDto.DefectEntry> list = buildDefects(names, values);
        if (list.isEmpty() && stageTotal > 0) {
            list.add(new RejectionBreakupDto.DefectEntry("Rejected", stageTotal));
        }
        return list;
    }

    private void addSection(List<RejectionBreakupDto.StageSection> sections, String name, List<RejectionBreakupDto.DefectEntry> defects) {
        int total = defects.stream().mapToInt(RejectionBreakupDto.DefectEntry::getRejectedQty).sum();
        if (total > 0) sections.add(new RejectionBreakupDto.StageSection(name, total, defects));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private IeScrapEntry getActiveEntryOrThrow(Integer id) {
        IeScrapEntry entry = scrapEntryRepo.findById(id)
                .orElseThrow(() -> new InvalidInputException(new ErrorDetails(404, 404, "error", "Scrap entry not found.")));
        if (STATUS_DELETED.equals(entry.getStatus())) {
            throw new InvalidInputException(new ErrorDetails(400, 400, "error", "Scrap entry has already been deleted."));
        }
        return entry;
    }

    private void assertWithinEditWindow(IeScrapEntry entry) {
        long minutes = ChronoUnit.MINUTES.between(entry.getCreatedDate(), LocalDateTime.now());
        if (minutes > EDIT_WINDOW_MINUTES) {
            throw new InvalidInputException(new ErrorDetails(400, 400, "error",
                    "Edit/Delete window of 1 hour has expired for this entry."));
        }
    }

    private void auditField(Integer entryId, String field, String oldVal, String newVal,
                            String reason, String actionBy) {
        if (Objects.equals(oldVal, newVal)) return;
        IeScrapEntryAudit audit = new IeScrapEntryAudit();
        audit.setScrapEntryId(entryId);
        audit.setAction("EDIT");
        audit.setFieldChanged(field);
        audit.setOldValue(oldVal);
        audit.setNewValue(newVal);
        audit.setReason(reason);
        audit.setActionBy(actionBy);
        audit.setActionAt(LocalDateTime.now());
        auditRepo.save(audit);
    }

    private String generateVerificationId() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        Long count = scrapEntryRepo.countTodayEntries();
        return "SC-" + date + String.format("%02d", count + 1);
    }

    private ScrapEntryResponseDto toResponseDto(IeScrapEntry entry, List<String> imageUrls) {
        ScrapEntryResponseDto dto = new ScrapEntryResponseDto();
        dto.setId(entry.getId());
        dto.setPoiCode(entry.getPoiCode());
        dto.setUnitLineShedNo(entry.getUnitLineShedNo());
        dto.setScrappingDate(entry.getScrappingDate());
        dto.setShift(entry.getShift());
        dto.setNoOfPiecesScrapped(entry.getNoOfPiecesScrapped());
        dto.setPendingQtyAtSubmit(entry.getPendingQtyAtSubmit());
        dto.setDeclarationAccepted(entry.getDeclarationAccepted());
        dto.setRemarks(entry.getRemarks());
        dto.setScrapVerificationId(entry.getScrapVerificationId());
        dto.setIeUserId(entry.getIeUserId());
        dto.setIeType(entry.getIeType());
        dto.setStatus(entry.getStatus());
        dto.setCreatedBy(entry.getCreatedBy());
        dto.setCreatedDate(entry.getCreatedDate());
        dto.setUpdatedBy(entry.getUpdatedBy());
        dto.setUpdatedDate(entry.getUpdatedDate());
        dto.setImageUrls(imageUrls);
        dto.setEditable(ChronoUnit.MINUTES.between(entry.getCreatedDate(), LocalDateTime.now()) <= EDIT_WINDOW_MINUTES);
        return dto;
    }

    private String str(Object o) { return o == null ? null : o.toString(); }
}
