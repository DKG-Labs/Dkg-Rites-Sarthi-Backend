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
import com.sarthi.repository.PoHeaderRepository;
import com.sarthi.repository.UserMasterRepository;
import com.sarthi.repository.rawmaterial.RmHeatQuantityRepository;
import com.sarthi.service.VendorInspectionCallService;
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
                    .stream().collect(Collectors.toMap(UserMaster::getUserId, UserMaster::getFullName));
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
                    .stream().collect(Collectors.groupingBy(hq -> hq.getRmInspectionDetails().getId(), Collectors.counting()));
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
                            ld -> ld.getLotNumber(),
                            (ld1, ld2) -> ld1 // Take first lot
                    ));
        }
        logger.info("Step 3d: Fetched extra details (Heat count/Lots) in {}ms", (System.currentTimeMillis() - stepStart));

        // 4. Map each inspection call to DTO using bulk-fetched data
        stepStart = System.currentTimeMillis();
        final Map<Integer, String> finalUserNamesMap = userNamesMap;
        final Map<Long, Long> finalRmHeatCountMap = rmHeatCountMap;
        final Map<Long, String> finalFinalLotNoMap = finalLotNoMap;

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
                        finalFinalLotNoMap))
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
            Map<Long, String> finalLotNoMap) {

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
            return processList.get(0).getOfferedQty();
        } else if ("Final".equalsIgnoreCase(ic.getTypeOfCall()) && finalDetails != null) {
            return finalDetails.getTotalOfferedQty();
        }
        return 0;
    }

    // Deprecated methods replaced by optimized versions
}
