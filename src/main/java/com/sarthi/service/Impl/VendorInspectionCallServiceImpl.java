package com.sarthi.service.Impl;

import com.sarthi.dto.VendorInspectionCallStatusDto;
import com.sarthi.entity.WorkflowTransition;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.entity.rawmaterial.RmInspectionDetails;
import com.sarthi.entity.processmaterial.ProcessInspectionDetails;
import com.sarthi.entity.finalmaterial.FinalInspectionDetails;
import com.sarthi.entity.PoHeader;
import com.sarthi.entity.UserMaster;
import com.sarthi.repository.WorkflowTransitionRepository;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.repository.finalmaterial.FinalInspectionLotDetailsRepository;
import com.sarthi.repository.finalmaterial.FinalCumulativeResultsRepository;
import com.sarthi.repository.RmHeatFinalResultRepository;
import com.sarthi.repository.processmaterial.ProcessLineFinalResultRepository;
import com.sarthi.repository.PoHeaderRepository;
import com.sarthi.repository.UserMasterRepository;
import com.sarthi.repository.rawmaterial.RmHeatQuantityRepository;
import com.sarthi.repository.InventoryEntryRepository;
import com.sarthi.service.AzureBlobStorageService;
import com.sarthi.service.VendorInspectionCallService;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.constant.AppConstant;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;
import java.io.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service implementation for Vendor Inspection Call operations.
 */
@Service
public class VendorInspectionCallServiceImpl implements VendorInspectionCallService {

    private static final Logger logger = LoggerFactory.getLogger(VendorInspectionCallServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private InspectionCallRepository inspectionCallRepository;

    @Autowired
    private WorkflowTransitionRepository workflowTransitionRepository;

    @Autowired
    private PoHeaderRepository poHeaderRepository;

    @Autowired
    private UserMasterRepository userMasterRepository;

    @Autowired
    private RmHeatQuantityRepository rmHeatQuantityRepository;

    @Autowired
    private FinalInspectionLotDetailsRepository finalInspectionLotDetailsRepository;

    @Autowired
    private InventoryEntryRepository inventoryEntryRepository;

    @Autowired
    private AzureBlobStorageService azureBlobStorageService;

    @Autowired
    private RmHeatFinalResultRepository rmHeatFinalResultRepository;

    @Autowired
    private ProcessLineFinalResultRepository processLineFinalResultRepository;

    @Autowired
    private FinalCumulativeResultsRepository finalCumulativeResultsRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VendorInspectionCallStatusDto> getVendorInspectionCallsWithStatus(String vendorId) {
        logger.info("Fetching inspection calls with workflow status for vendor: {}", vendorId);

        long startTime = System.currentTimeMillis();

        // 1. Fetch all inspection calls for the vendor
        long stepStart = System.currentTimeMillis();
        List<InspectionCall> inspectionCalls = inspectionCallRepository.findByVendorIdOrderByCreatedAtDesc(vendorId);
        logger.info("Step 1: Fetched {} inspection calls in {}ms", inspectionCalls.size(), (System.currentTimeMillis() - stepStart));

        if (inspectionCalls.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Collect all necessary IDs for bulk fetching
        List<String> icNumbers = inspectionCalls.stream().map(InspectionCall::getIcNumber).collect(Collectors.toList());
        List<String> poNos = inspectionCalls.stream().map(InspectionCall::getPoNo).distinct().collect(Collectors.toList());

        // 3. Perform bulk fetches
        // Latest Transitions
        stepStart = System.currentTimeMillis();
        Map<String, WorkflowTransition> transitionMap = workflowTransitionRepository.findLatestByRequestIds(icNumbers)
                .stream().collect(Collectors.toMap(WorkflowTransition::getRequestId, wt -> wt, (wt1, wt2) -> wt1));
        logger.info("Step 3a: Fetched {} latest transitions in {}ms", transitionMap.size(), (System.currentTimeMillis() - stepStart));

        // PO Headers
        stepStart = System.currentTimeMillis();
        Map<String, PoHeader> poMap = poHeaderRepository.findByPoNoIn(poNos)
                .stream().collect(Collectors.toMap(PoHeader::getPoNo, ph -> ph, (ph1, ph2) -> ph1));
        logger.info("Step 3b: Fetched {} PO headers in {}ms", poMap.size(), (System.currentTimeMillis() - stepStart));

        // Note: Inspection Details (RM, Process, Final) are already eager-loaded via EntityGraph on findByVendorId

        // User Details (IE Names)
        stepStart = System.currentTimeMillis();
        Set<Integer> userIds = transitionMap.values().stream()
                .flatMap(wt -> Stream.of(wt.getAssignedToUser(), wt.getProcessIeUserId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, String> userNamesMap = Collections.emptyMap();
        if (!userIds.isEmpty()) {
            userNamesMap = userMasterRepository.findByUserIdIn(new ArrayList<>(userIds))
                    .stream().collect(Collectors.toMap(
                            UserMaster::getUserId,
                            u -> Optional.ofNullable(u.getFullName()).orElse("Unknown")
                    ));
        }
        logger.info("Step 3c: Fetched {} user names in {}ms", userNamesMap.size(), (System.currentTimeMillis() - stepStart));

        // Additional nested data
        // RM Heat Quantities
        stepStart = System.currentTimeMillis();
        List<Long> rmDetailIds = inspectionCalls.stream()
                .map(InspectionCall::getRmInspectionDetails)
                .filter(Objects::nonNull)
                .map(RmInspectionDetails::getId)
                .collect(Collectors.toList());

        Map<Long, Long> rmHeatCountMap = Collections.emptyMap();
        if (!rmDetailIds.isEmpty()) {
            rmHeatCountMap = rmHeatQuantityRepository.findByRmInspectionDetailsIdIn(rmDetailIds)
                    .stream()
                    .filter(hq -> hq.getHeatNumber() != null && !hq.getHeatNumber().trim().isEmpty())
                    .collect(Collectors.groupingBy(
                            hq -> hq.getRmInspectionDetails().getId(),
                            Collectors.mapping(
                                    hq -> hq.getHeatNumber().trim(),
                                    Collectors.collectingAndThen(Collectors.toSet(), set -> (long) set.size())
                            )
                    ));
        }

        // Final Lot Details
        List<Long> finalDetailIds = inspectionCalls.stream()
                .map(InspectionCall::getFinalInspectionDetails)
                .filter(Objects::nonNull)
                .map(FinalInspectionDetails::getId)
                .collect(Collectors.toList());

        Map<Long, String> finalLotNoMap = Collections.emptyMap();
        if (!finalDetailIds.isEmpty()) {
            finalLotNoMap = finalInspectionLotDetailsRepository.findByFinalDetailIdIn(finalDetailIds)
                    .stream().collect(Collectors.toMap(
                            ld -> ld.getFinalDetailId(),
                            ld -> Optional.ofNullable(ld.getLotNumber()).orElse("N/A"),
                            (ld1, ld2) -> ld1 // Take first lot
                    ));
        }
        logger.info("Step 3d: Fetched extra details (Heat count/Lots) in {}ms", (System.currentTimeMillis() - stepStart));

        // 3e. Bulk-fetch actual accepted quantities from result tables
        stepStart = System.currentTimeMillis();
        List<String> rmIcNumbers = inspectionCalls.stream()
                .filter(ic -> "Raw Material".equalsIgnoreCase(ic.getTypeOfCall()))
                .map(InspectionCall::getIcNumber).collect(Collectors.toList());
        List<String> processIcNumbers = inspectionCalls.stream()
                .filter(ic -> "Process".equalsIgnoreCase(ic.getTypeOfCall()))
                .map(InspectionCall::getIcNumber).collect(Collectors.toList());
        List<String> finalIcNumbers = inspectionCalls.stream()
                .filter(ic -> "Final".equalsIgnoreCase(ic.getTypeOfCall()))
                .map(InspectionCall::getIcNumber).collect(Collectors.toList());

        Map<String, Long> acceptedQtyMap = new HashMap<>();
        if (!rmIcNumbers.isEmpty()) {
            rmHeatFinalResultRepository.sumAcceptedQtyByIcNumbers(rmIcNumbers)
                    .forEach(row -> acceptedQtyMap.put((String) row[0], ((Number) row[1]).longValue()));
        }
        if (!processIcNumbers.isEmpty()) {
            processLineFinalResultRepository.sumAcceptedQtyByIcNumbers(processIcNumbers)
                    .forEach(row -> acceptedQtyMap.put((String) row[0], ((Number) row[1]).longValue()));
        }
        if (!finalIcNumbers.isEmpty()) {
            finalCumulativeResultsRepository.sumAcceptedQtyByIcNumbers(finalIcNumbers)
                    .forEach(row -> acceptedQtyMap.put((String) row[0], ((Number) row[1]).longValue()));
        }
        logger.info("Step 3e: Fetched accepted qtys for {} calls in {}ms", acceptedQtyMap.size(), (System.currentTimeMillis() - stepStart));

        // 4. Map each inspection call to DTO using bulk-fetched data
        stepStart = System.currentTimeMillis();
        final Map<Integer, String> finalUserNamesMap = userNamesMap;
        final Map<Long, Long> finalRmHeatCountMap = rmHeatCountMap;
        final Map<Long, String> finalFinalLotNoMap = finalLotNoMap;
        final Map<String, Long> finalAcceptedQtyMap = acceptedQtyMap;

        List<VendorInspectionCallStatusDto> results = inspectionCalls.stream()
                .map(ic -> mapToVendorInspectionCallStatusDtoOptimized(
                        ic,
                        transitionMap.get(ic.getIcNumber()),
                        poMap.get(ic.getPoNo()),
                        ic.getRmInspectionDetails(),
                        ic.getProcessInspectionDetails(),
                        ic.getFinalInspectionDetails(),
                        finalUserNamesMap,
                        finalRmHeatCountMap,
                        finalFinalLotNoMap,
                        finalAcceptedQtyMap))
                .collect(Collectors.toList());

        long endTime = System.currentTimeMillis();
        logger.info("Successfully fetched {} inspection calls for vendor: {} in {}ms", results.size(), vendorId, (endTime - startTime));

        return results;
    }

    /**
     * Optimized mapping from InspectionCall entity to VendorInspectionCallStatusDto
     */
    private VendorInspectionCallStatusDto mapToVendorInspectionCallStatusDtoOptimized(
            InspectionCall ic,
            WorkflowTransition latestTransition,
            PoHeader ph,
            RmInspectionDetails rmDetails,
            List<ProcessInspectionDetails> processList,
            FinalInspectionDetails finalDetails,
            Map<Integer, String> userNamesMap,
            Map<Long, Long> rmHeatCountMap,
            Map<Long, String> finalLotNoMap,
            Map<String, Long> acceptedQtyMap) {

        // Get item name and quantity based on type of call
        String itemName = getItemNameOptimized(ic, rmDetails, processList, finalDetails);
        Integer quantityOffered = getQuantityOfferedOptimized(ic, rmDetails, processList, finalDetails);

        // Fetch PoHeader details
        String rlyShortName = ph != null ? ph.getRlyShortName() : "N/A";
        String rlyCd = ph != null ? ph.getRlyCd() : "N/A";

        // IE Name from Map
        String ieName = "Not Assigned";
        if (latestTransition != null) {
            if (latestTransition.getAssignedToUser() != null) {
                ieName = userNamesMap.getOrDefault(latestTransition.getAssignedToUser(), "Not Assigned");
            } else if (latestTransition.getProcessIeUserId() != null) {
                ieName = userNamesMap.getOrDefault(latestTransition.getProcessIeUserId(), "Not Assigned");
            }
        }

        // Get Heats/Lots count
        Integer noOfHeatsRM = null;
        String lotNoProcess = null;
        String lotNoFinal = null;
        String uom = "N/A";

        if ("Raw Material".equalsIgnoreCase(ic.getTypeOfCall()) && rmDetails != null) {
            Long count = rmHeatCountMap.get(rmDetails.getId());
            noOfHeatsRM = count != null ? count.intValue() : 0;
            uom = rmDetails.getUnitOfMeasurement();
        } else if ("Process".equalsIgnoreCase(ic.getTypeOfCall()) && processList != null && !processList.isEmpty()) {
            lotNoProcess = processList.get(0).getLotNumber();
        } else if ("Final".equalsIgnoreCase(ic.getTypeOfCall()) && finalDetails != null) {
            lotNoFinal = finalLotNoMap.get(finalDetails.getId());
        }

        String scheduledDate = null;
        if (latestTransition != null && "SCHEDULED".equalsIgnoreCase(latestTransition.getStatus())) {
            scheduledDate = ic.getActualInspectionDate() != null ? ic.getActualInspectionDate().format(DATE_FORMATTER) : null;
        }

        return VendorInspectionCallStatusDto.builder()
                .workflowTransitionId(latestTransition != null ? latestTransition.getWorkflowTransitionId() : null)
                .icNumber(ic.getIcNumber())
                .poNo(ic.getPoNo())
                .poSerialNo(ic.getPoSerialNo())
                .typeOfCall(ic.getTypeOfCall())
                .desiredInspectionDate(ic.getDesiredInspectionDate() != null ? ic.getDesiredInspectionDate().format(DATE_FORMATTER) : null)
                .placeOfInspection(ic.getPlaceOfInspection())
                .itemName(itemName)
                .quantityOffered(quantityOffered)
                .workflowStatus(latestTransition != null ? latestTransition.getStatus() : ic.getStatus())
                .currentRoleName(latestTransition != null ? latestTransition.getCurrentRoleName() : null)
                .nextRoleName(latestTransition != null ? latestTransition.getNextRoleName() : null)
                .jobStatus(latestTransition != null ? latestTransition.getJobStatus() : null)
                .companyName(ic.getCompanyName())
                .unitName(ic.getUnitName())
                .createdAt(ic.getCreatedAt() != null ? ic.getCreatedAt().format(DATE_FORMATTER) : null)
                .updatedAt(ic.getUpdatedAt() != null ? ic.getUpdatedAt().format(DATE_FORMATTER) : null)
                .rlyShortName(rlyShortName)
                .rlyCd(rlyCd)
                .ercType(ic.getErcType())
                .noOfHeatsRM(noOfHeatsRM)
                .lotNoProcess(lotNoProcess)
                .lotNoFinal(lotNoFinal)
                .ieName(ieName)
                .uom(uom)
                .scheduledDate(scheduledDate)
                .acceptedQty(acceptedQtyMap.getOrDefault(ic.getIcNumber(), 0L))
                .build();
    }

    private String getItemNameOptimized(InspectionCall ic, RmInspectionDetails rmDetails, List<ProcessInspectionDetails> processList, FinalInspectionDetails finalDetails) {
        if ("Raw Material".equalsIgnoreCase(ic.getTypeOfCall()) && rmDetails != null) {
            return rmDetails.getItemDescription();
        } else if ("Process".equalsIgnoreCase(ic.getTypeOfCall()) && processList != null && !processList.isEmpty()) {
            return "Process Inspection - Lot: " + processList.get(0).getLotNumber();
        } else if ("Final".equalsIgnoreCase(ic.getTypeOfCall()) && finalDetails != null) {
            return "Final Inspection - " + finalDetails.getTotalLots() + " lots";
        }
        return "N/A";
    }

    private Integer getQuantityOfferedOptimized(InspectionCall ic, RmInspectionDetails rmDetails, List<ProcessInspectionDetails> processList, FinalInspectionDetails finalDetails) {
        if ("Raw Material".equalsIgnoreCase(ic.getTypeOfCall()) && rmDetails != null) {
            return rmDetails.getOfferedQtyErc();
        } else if ("Process".equalsIgnoreCase(ic.getTypeOfCall()) && processList != null && !processList.isEmpty()) {
            return processList.stream().filter(p -> p.getOfferedQty() != null).mapToInt(p -> p.getOfferedQty()).sum();
        } else if ("Final".equalsIgnoreCase(ic.getTypeOfCall()) && finalDetails != null) {
            return finalDetails.getTotalOfferedQty();
        }
        return 0;
    }

    // Deprecated methods replaced by optimized versions

    @Autowired
    private com.sarthi.repository.rawmaterial.RmInspectionDetailsRepository rmInspectionDetailsRepository;

    @Override
    @Transactional(readOnly = true)
    public byte[] getTcDocsByCallNo(String callNo) {
        if (callNo == null || callNo.isBlank()) {
            throw new BusinessException(new ErrorDetails(AppConstant.NO_RECORD_FOUND, AppConstant.ERROR_TYPE_CODE_VALIDATION, AppConstant.ERROR_TYPE_VALIDATION, "Call number cannot be empty"));
        }
        String trimmed = callNo.trim();
        logger.info("Fetching TC docs for call number: {}", trimmed);

        Set<String> heatNumbers = new LinkedHashSet<>();
        Set<String> tcNumbers = new LinkedHashSet<>();
        Set<String> tcFilePaths = new LinkedHashSet<>();

        // Step 1: Find InspectionCall by IC number variants
        List<String> callVariants = new ArrayList<>();
        callVariants.add(trimmed);
        if (trimmed.contains("-")) {
            callVariants.add(trimmed.substring(trimmed.indexOf("-") + 1).trim());
        }
        if (!trimmed.startsWith("ER-")) {
            callVariants.add("ER-" + trimmed);
        }

        InspectionCall matchedCall = null;
        for (String variant : callVariants) {
            if (variant.isBlank()) continue;
            Optional<InspectionCall> opt = inspectionCallRepository.findByIcNumber(variant);
            if (opt.isPresent()) {
                matchedCall = opt.get();
                logger.info("Found InspectionCall with ID: {} for variant: {}", matchedCall.getId(), variant);
                break;
            }
        }

        // Step 2: From InspectionCall -> RmInspectionDetails -> RmHeatQuantity
        if (matchedCall != null) {
            RmInspectionDetails rmDetails = matchedCall.getRmInspectionDetails();
            if (rmDetails == null) {
                rmDetails = rmInspectionDetailsRepository.findByIcId(matchedCall.getId()).orElse(null);
            }

            if (rmDetails != null) {
                logger.info("Found RmInspectionDetails ID: {} for InspectionCall ID: {}", rmDetails.getId(), matchedCall.getId());
                if (rmDetails.getTcNumber() != null && !rmDetails.getTcNumber().isBlank()) {
                    tcNumbers.add(rmDetails.getTcNumber().trim());
                }
                if (rmDetails.getHeatNumbers() != null && !rmDetails.getHeatNumbers().isBlank()) {
                    for (String hn : rmDetails.getHeatNumbers().split(",")) {
                        if (!hn.trim().isBlank()) heatNumbers.add(hn.trim());
                    }
                }

                List<com.sarthi.entity.rawmaterial.RmHeatQuantity> hqList = 
                        rmHeatQuantityRepository.findByRmDetailId(Math.toIntExact(rmDetails.getId()));
                if (hqList != null && !hqList.isEmpty()) {
                    for (com.sarthi.entity.rawmaterial.RmHeatQuantity hq : hqList) {
                        if (hq.getHeatNumber() != null && !hq.getHeatNumber().isBlank()) {
                            heatNumbers.add(hq.getHeatNumber().trim());
                        }
                        if (hq.getTcNumber() != null && !hq.getTcNumber().isBlank()) {
                            tcNumbers.add(hq.getTcNumber().trim());
                        }
                    }
                }
            }
        }

        // Step 3: Fallback query via RmHeatQuantityRepository
        if (heatNumbers.isEmpty() && tcNumbers.isEmpty()) {
            for (String variant : callVariants) {
                List<com.sarthi.entity.rawmaterial.RmHeatQuantity> hqList = 
                        rmHeatQuantityRepository.findByInspectionCallNo(variant);
                if (hqList != null && !hqList.isEmpty()) {
                    for (com.sarthi.entity.rawmaterial.RmHeatQuantity hq : hqList) {
                        if (hq.getHeatNumber() != null && !hq.getHeatNumber().isBlank()) {
                            heatNumbers.add(hq.getHeatNumber().trim());
                        }
                        if (hq.getTcNumber() != null && !hq.getTcNumber().isBlank()) {
                            tcNumbers.add(hq.getTcNumber().trim());
                        }
                    }
                    break;
                }
            }
        }

        logger.info("Resolved Heat Numbers: {} and TC Numbers: {} for call: {}", heatNumbers, tcNumbers, trimmed);

        // Step 4: Find inventory entries by heat numbers
        if (!heatNumbers.isEmpty()) {
            List<com.sarthi.entity.InventoryEntry> entries = inventoryEntryRepository.findByHeatNumberIn(new ArrayList<>(heatNumbers));
            if (entries != null) {
                for (com.sarthi.entity.InventoryEntry ie : entries) {
                    if (ie.getTcFilePath() != null && !ie.getTcFilePath().isBlank()) {
                        tcFilePaths.add(ie.getTcFilePath().trim());
                    }
                }
            }
        }

        // Step 5: Fallback to repository native query
        if (tcFilePaths.isEmpty()) {
            for (String variant : callVariants) {
                List<String> paths = inventoryEntryRepository.findTcFilePathsByCallNo(variant);
                if (paths != null && !paths.isEmpty()) {
                    tcFilePaths.addAll(paths);
                    break;
                }
            }
        }

        logger.info("Found {} TC file paths for call {}: {}", tcFilePaths.size(), trimmed, tcFilePaths);

        if (tcFilePaths.isEmpty()) {
            throw new BusinessException(new ErrorDetails(AppConstant.NO_RECORD_FOUND, AppConstant.ERROR_TYPE_CODE_VALIDATION, AppConstant.ERROR_TYPE_VALIDATION, "No TC Documents found for this call number"));
        }

        // Step 6: Download all TC PDF files from Azure Storage
        List<byte[]> pdfBytesList = new ArrayList<>();
        for (String path : tcFilePaths) {
            try {
                if (path == null || path.isBlank()) continue;
                String blobName = path.substring(path.lastIndexOf('/') + 1);
                blobName = java.net.URLDecoder.decode(blobName, java.nio.charset.StandardCharsets.UTF_8);
                byte[] pdfBytes = azureBlobStorageService.downloadFile(blobName);
                if (pdfBytes != null && pdfBytes.length > 0) {
                    pdfBytesList.add(pdfBytes);
                }
            } catch (Exception e) {
                logger.error("Failed to download TC document from blob: {}", path, e);
            }
        }

        if (pdfBytesList.isEmpty()) {
            throw new BusinessException(new ErrorDetails(AppConstant.NO_RECORD_FOUND, AppConstant.ERROR_TYPE_CODE_VALIDATION, AppConstant.ERROR_TYPE_VALIDATION, "Failed to download any TC Documents"));
        }
        
        if (pdfBytesList.size() == 1) {
            return pdfBytesList.get(0);
        }

        // Step 7: Merge multiple TC files into a single PDF
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfCopy copy = new PdfCopy(document, baos);
            document.open();
            for (byte[] pdf : pdfBytesList) {
                PdfReader reader = new PdfReader(pdf);
                for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                    copy.addPage(copy.getImportedPage(reader, i));
                }
                reader.close();
            }
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("Error merging PDF documents for call: {}", trimmed, e);
            throw new BusinessException(new ErrorDetails(AppConstant.INTERNAL_SERVER_ERROR, AppConstant.ERROR_TYPE_CODE_INTERNAL, AppConstant.ERROR_TYPE_INTERNAL, "Error merging TC documents"));
        }
    }
}