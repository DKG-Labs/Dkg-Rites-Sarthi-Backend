package com.sarthi.service.Impl;

import com.sarthi.dto.VendorInspectionCallStatusDto;
import com.sarthi.entity.WorkflowTransition;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.entity.rawmaterial.RmInspectionDetails;
import com.sarthi.entity.processmaterial.ProcessInspectionDetails;
import com.sarthi.entity.finalmaterial.FinalInspectionDetails;
import com.sarthi.repository.WorkflowTransitionRepository;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.repository.rawmaterial.RmInspectionDetailsRepository;
import com.sarthi.repository.processmaterial.ProcessInspectionDetailsRepository;
import com.sarthi.repository.finalmaterial.FinalInspectionDetailsRepository;
import com.sarthi.service.VendorInspectionCallService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private RmInspectionDetailsRepository rmInspectionDetailsRepository;

    @Autowired
    private ProcessInspectionDetailsRepository processInspectionDetailsRepository;

    @Autowired
    private FinalInspectionDetailsRepository finalInspectionDetailsRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VendorInspectionCallStatusDto> getVendorInspectionCallsWithStatus(String vendorId) {
        logger.info("Fetching inspection calls with workflow status for vendor: {}", vendorId);

       Long t1 =System.currentTimeMillis();
        System.out.println(t1);
        // Fetch all inspection calls for the vendor
        List<InspectionCall> inspectionCalls = inspectionCallRepository.findByVendorIdOrderByCreatedAtDesc(vendorId);
        Long t2 =System.currentTimeMillis();

        System.out.println( t1-t2);
        logger.info("Found {} inspection calls for vendor: {}", inspectionCalls.size(), vendorId);

        // Map each inspection call to DTO with workflow status
        return inspectionCalls.stream()
                .map(this::mapToVendorInspectionCallStatusDto)
                .collect(Collectors.toList());
    }

    /**
     * Map InspectionCall entity to VendorInspectionCallStatusDto with workflow status
     */
    private VendorInspectionCallStatusDto mapToVendorInspectionCallStatusDto(InspectionCall ic) {
        // Get latest workflow transition for this IC
        WorkflowTransition latestTransition = workflowTransitionRepository
                .findTopByRequestIdOrderByWorkflowTransitionIdDesc(ic.getIcNumber());

        // Get item name and quantity based on type of call
        String itemName = getItemName(ic);
        Integer quantityOffered = getQuantityOffered(ic);

        return VendorInspectionCallStatusDto.builder()
                .icNumber(ic.getIcNumber())
                .poNo(ic.getPoNo())
                .poSerialNo(ic.getPoSerialNo())
                .typeOfCall(ic.getTypeOfCall())
                .desiredInspectionDate(ic.getDesiredInspectionDate() != null ? 
                        ic.getDesiredInspectionDate().format(DATE_FORMATTER) : null)
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
                .build();
    }

    /**
     * Get item name based on inspection type
     */
    private String getItemName(InspectionCall ic) {
        try {
            if ("Raw Material".equalsIgnoreCase(ic.getTypeOfCall())) {
                return rmInspectionDetailsRepository.findByIcId(ic.getId())
                        .map(RmInspectionDetails::getItemDescription)
                        .orElse("N/A");
            } else if ("Process".equalsIgnoreCase(ic.getTypeOfCall())) {
                List<ProcessInspectionDetails> processList = processInspectionDetailsRepository.findByIcId(ic.getId());
                if (!processList.isEmpty()) {
                    return "Process Inspection - Lot: " + processList.get(0).getLotNumber();
                }
                return "N/A";
            } else if ("Final".equalsIgnoreCase(ic.getTypeOfCall())) {
                return finalInspectionDetailsRepository.findByIcId(ic.getId())
                        .map(details -> "Final Inspection - " + details.getTotalLots() + " lots")
                        .orElse("N/A");
            }
        } catch (Exception e) {
            logger.warn("Error fetching item name for IC: {}", ic.getIcNumber(), e);
        }
        return "N/A";
    }

    /**
     * Get quantity offered based on inspection type
     */
    private Integer getQuantityOffered(InspectionCall ic) {
        try {
            if ("Raw Material".equalsIgnoreCase(ic.getTypeOfCall())) {
                return rmInspectionDetailsRepository.findByIcId(ic.getId())
                        .map(RmInspectionDetails::getOfferedQtyErc)
                        .orElse(0);
            } else if ("Process".equalsIgnoreCase(ic.getTypeOfCall())) {
                List<ProcessInspectionDetails> processList = processInspectionDetailsRepository.findByIcId(ic.getId());
                if (!processList.isEmpty()) {
                    return processList.get(0).getOfferedQty();
                }
                return 0;
            } else if ("Final".equalsIgnoreCase(ic.getTypeOfCall())) {
                return finalInspectionDetailsRepository.findByIcId(ic.getId())
                        .map(FinalInspectionDetails::getTotalOfferedQty)
                        .orElse(0);
            }
        } catch (Exception e) {
            logger.warn("Error fetching quantity for IC: {}", ic.getIcNumber(), e);
        }
        return 0;
    }
}

