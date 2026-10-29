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
import com.sarthi.repository.finalmaterial.FinalCumulativeResultsRepository;
import com.sarthi.repository.processmaterial.ProcessLineFinalResultRepository;
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
