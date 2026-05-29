package com.sarthi.service.rawmaterial.impl;

import com.sarthi.constant.AppConstant;
import com.sarthi.dto.rawmaterial.*;
import com.sarthi.entity.InspectionCompleteDetails;
import com.sarthi.entity.RmHeatFinalResult;
import com.sarthi.entity.rawmaterial.*;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.repository.InspectionCompleteDetailsRepository;
import com.sarthi.repository.RmHeatFinalResultRepository;
import com.sarthi.repository.rawmaterial.*;
import com.sarthi.service.rawmaterial.RawMaterialInspectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for Raw Material Inspection operations.
 * Handles data retrieval from 4 RM tables with proper entity-to-DTO mapping.
 */
@Service
@Transactional(readOnly = true)
public class RawMaterialInspectionServiceImpl implements RawMaterialInspectionService {

        private static final Logger logger = LoggerFactory.getLogger(RawMaterialInspectionServiceImpl.class);

        private final InspectionCallRepository inspectionCallRepository;
        private final RmInspectionDetailsRepository rmDetailsRepository;
        private final RmHeatQuantityRepository heatQuantityRepository;
        private final InspectionCompleteDetailsRepository inspectionCompleteDetailsRepository;
        private final RmHeatFinalResultRepository rmHeatFinalResultRepository;
        private final RmChemicalAnalysisRepository rmChemicalAnalysisRepository;

        @Autowired
        public RawMaterialInspectionServiceImpl(
                        InspectionCallRepository inspectionCallRepository,
                        RmInspectionDetailsRepository rmDetailsRepository,
                        RmHeatQuantityRepository heatQuantityRepository,
                        InspectionCompleteDetailsRepository inspectionCompleteDetailsRepository,
                        RmHeatFinalResultRepository rmHeatFinalResultRepository,
                        RmChemicalAnalysisRepository rmChemicalAnalysisRepository) {
                this.inspectionCallRepository = inspectionCallRepository;
                this.rmDetailsRepository = rmDetailsRepository;
                this.heatQuantityRepository = heatQuantityRepository;
                this.inspectionCompleteDetailsRepository = inspectionCompleteDetailsRepository;
                this.rmHeatFinalResultRepository = rmHeatFinalResultRepository;
                this.rmChemicalAnalysisRepository = rmChemicalAnalysisRepository;
        }

        /* ==================== Inspection Call Operations ==================== */

        @Override
        public List<InspectionCallDto> getAllRawMaterialCalls() {
                logger.info("Fetching all Raw Material inspection calls");
                return inspectionCallRepository.findAllRawMaterialCallsWithDetails()
                                .stream()
                                .map(this::mapToCallDto)
                                .collect(Collectors.toList());
        }

        @Override
        public List<InspectionCallDto> getRawMaterialCallsByStatus(String status) {
                logger.info("Fetching Raw Material calls by status: {}", status);
                return inspectionCallRepository.findRawMaterialCallsByStatusWithDetails(status)
                                .stream()
                                .map(this::mapToCallDto)
                                .collect(Collectors.toList());
        }

        @Override
        public InspectionCallDto getInspectionCallById(Integer id) {
                logger.info("Fetching inspection call by ID: {}", id);
                InspectionCall call = inspectionCallRepository.findById(id)
                                .orElseThrow(() -> createNotFoundException("Inspection call not found with ID: " + id));
                return mapToCallDtoWithDetails(call);
        }

        @Override
        public InspectionCallDto getInspectionCallByIcNumber(String icNumber) {
                logger.info("Fetching inspection call by IC number: {}", icNumber);
                InspectionCall call = inspectionCallRepository.findByIcNumber(icNumber)
                                .orElseThrow(() -> createNotFoundException("Inspection call not found: " + icNumber));
                return mapToCallDtoWithDetails(call);
        }

        /* ==================== RM Inspection Details Operations ==================== */

        @Override
        public RmInspectionDetailsDto getRmDetailsByCallId(Integer inspectionCallId) {
                logger.info("Fetching RM details for call ID: {}", inspectionCallId);
                return rmDetailsRepository.findByIcId(Long.valueOf(inspectionCallId))
                                .map(this::mapToDetailsDto)
                                .orElse(null);
        }

        /* ==================== Heat Quantity Operations ==================== */

        @Override
        public List<RmHeatQuantityDto> getHeatQuantitiesByRmDetailId(Integer rmDetailId) {
                logger.info("Fetching heat quantities for RM detail ID: {}", rmDetailId);
                return heatQuantityRepository.findByRmDetailId(rmDetailId)
                                .stream()
                                .map(this::mapToHeatDto)
                                .collect(Collectors.toList());
        }

        @Override
        public RmHeatQuantityDto getHeatQuantityById(Integer heatId) {
                logger.info("Fetching heat quantity by ID: {}", heatId);
                RmHeatQuantity heat = heatQuantityRepository.findById(heatId)
                                .orElseThrow(() -> createNotFoundException(
                                                "Heat quantity not found with ID: " + heatId));
                return mapToHeatDto(heat);
        }

        /* ==================== Process IC Support Operations ==================== */

        @Override
        public List<String> getCompletedRmIcNumbers(String poSerialNo) {
                logger.info("Fetching completed RM IC certificate numbers for PO Serial No: {}", poSerialNo);

                // If PO Serial Number is provided, use the optimized query with PO Serial
                // filter
                if (poSerialNo != null && !poSerialNo.trim().isEmpty()) {
                        logger.info("Using PO Serial-filtered query for PO Serial No: {}", poSerialNo);
                        return inspectionCompleteDetailsRepository
                                        .findCompletedRmIcCertificateNumbersByPoSerialNo(poSerialNo);
                }

                // Otherwise, fall back to the original logic (fetch all ER ICs)
                logger.info("No PO Serial filter provided, fetching all ER ICs");
                List<InspectionCompleteDetails> completedDetails = inspectionCompleteDetailsRepository.findAll();

                // Filter by ER prefix (Raw Material inspections) and return certificate numbers
                return completedDetails.stream()
                                .filter(detail -> detail.getCallNo() != null && detail.getCallNo().startsWith("ER-"))
                                .map(InspectionCompleteDetails::getCertificateNo)
                                .filter(certNo -> certNo != null && !certNo.isEmpty())
                                .distinct()
                                .collect(Collectors.toList());
        }

        @Override
        public List<RmHeatQuantityDto> getHeatNumbersByRmIcNumber(String erNumber) {
                logger.info("Fetching heat numbers for ER number: {}", erNumber);

                // 1. Find the inspection call by ER number (ic_number column in
                // inspection_calls table)
                InspectionCall inspectionCall = inspectionCallRepository.findByIcNumber(erNumber)
                                .orElseThrow(() -> createNotFoundException(
                                                "ER number not found in inspection_calls: " + erNumber));

                logger.info("Found inspection call with id: {} for ER number: {}", inspectionCall.getId(), erNumber);

                // 2. Get the RM inspection details for this call using ic_id
                RmInspectionDetails rmDetails = rmDetailsRepository.findByIcId(inspectionCall.getId())
                                .orElseThrow(() -> createNotFoundException(
                                                "RM details not found for ER number: " + erNumber));

                logger.info("Found RM inspection details with id: {} for ic_id: {}", rmDetails.getId(),
                                inspectionCall.getId());

                // 3. Get all heat quantities for this RM detail using rm_detail_id
                List<RmHeatQuantity> heatQuantities = heatQuantityRepository
                                .findByRmDetailId(Math.toIntExact(rmDetails.getId()));

                logger.info("Found {} heat quantities for RM detail id: {}", heatQuantities.size(), rmDetails.getId());

                // 4. Map to DTOs and enrich with final result data
                return heatQuantities.stream()
                                .map(heatQty -> mapToHeatDtoWithFinalResult(heatQty, erNumber))
                                .collect(Collectors.toList());
        }

        @Override
        public InspectionCallDto getInspectionCallByCertificateNo(String certificateNo) {
                logger.info("========== START: Fetching inspection call by certificate number: {} ==========",
                                certificateNo);

                try {
                        // 1. Find the inspection complete details by certificate number
                        logger.info("Step 1: Searching for InspectionCompleteDetails with certificateNo: {}",
                                        certificateNo);
                        InspectionCompleteDetails completeDetails = inspectionCompleteDetailsRepository
                                        .findByCertificateNo(certificateNo)
                                        .orElseThrow(() -> {
                                                logger.error("❌ Certificate not found in inspection_complete_details: {}",
                                                                certificateNo);
                                                return createNotFoundException(
                                                                "Certificate not found: " + certificateNo);
                                        });

                        logger.info("✅ Step 1 SUCCESS: Found inspection complete details");
                        logger.info("   - Certificate No: {}", completeDetails.getCertificateNo());
                        logger.info("   - Call No (IC Number): {}", completeDetails.getCallNo());
                        logger.info("   - PO No: {}", completeDetails.getPoNo());

                        // 2. Get the IC number from the call_no field
                        String icNumber = completeDetails.getCallNo();
                        logger.info("Step 2: Extracted IC number from call_no: {}", icNumber);

                        // 3. Find the inspection call by IC number
                        logger.info("Step 3: Searching for InspectionCall with icNumber: {}", icNumber);
                        InspectionCall inspectionCall = inspectionCallRepository.findByIcNumber(icNumber)
                                        .orElseThrow(() -> {
                                                logger.error("❌ Inspection call not found for IC number: {}", icNumber);
                                                return createNotFoundException(
                                                                "Inspection call not found for IC number: " + icNumber);
                                        });

                        logger.info("✅ Step 3 SUCCESS: Found inspection call");
                        logger.info("   - IC ID: {}", inspectionCall.getId());
                        logger.info("   - IC Number: {}", inspectionCall.getIcNumber());
                        logger.info("   - Company ID: {}", inspectionCall.getCompanyId());
                        logger.info("   - Company Name: {}", inspectionCall.getCompanyName());
                        logger.info("   - Unit ID: {}", inspectionCall.getUnitId());
                        logger.info("   - Unit Name: {}", inspectionCall.getUnitName());

                        // 4. Map to DTO with all details
                        logger.info("Step 4: Mapping InspectionCall to DTO");
                        InspectionCallDto result = mapToCallDtoWithDetails(inspectionCall);

                        logger.info("✅ Step 4 SUCCESS: Mapped to DTO");
                        logger.info("   - DTO Company ID: {}", result.getCompanyId());
                        logger.info("   - DTO Unit ID: {}", result.getUnitId());
                        logger.info("========== END: Successfully fetched inspection call by certificate number ==========");

                        return result;
                } catch (Exception e) {
                        logger.error("❌ ERROR in getInspectionCallByCertificateNo: {}", e.getMessage(), e);
                        throw e;
                }
        }

        /* ==================== Chemical Analysis Operations ==================== */

        @Override
        public RmChemicalAnalysisDto getChemicalAnalysisByHeatNumber(String heatNumber) {
                logger.info("Fetching chemical analysis for heat number: {}", heatNumber);

                // Fetch all chemical analyses for this heat number, ordered by created_at DESC
                List<RmChemicalAnalysis> analyses = rmChemicalAnalysisRepository
                                .findByHeatNumberOrderByCreatedAtDesc(heatNumber);

                if (analyses.isEmpty()) {
                        logger.info("No chemical analysis found for heat number: {}", heatNumber);
                        return null;
                }

                // Get the most recent one (first in the list)
                RmChemicalAnalysis mostRecent = analyses.get(0);
                logger.info("Found chemical analysis for heat number: {} (created at: {})", heatNumber,
                                mostRecent.getCreatedAt());

                // Map to DTO
                return new RmChemicalAnalysisDto(
                                mostRecent.getHeatNumber(),
                                mostRecent.getCarbon(),
                                mostRecent.getManganese(),
                                mostRecent.getSilicon(),
                                mostRecent.getSulphur(),
                                mostRecent.getPhosphorus(),
                                mostRecent.getChromium());
        }

        /* ==================== Private Mapping Methods ==================== */

        /**
         * Maps InspectionCall entity to DTO (basic fields only)
         */
        private InspectionCallDto mapToCallDto(InspectionCall entity) {
                InspectionCallDto dto = InspectionCallDto.builder()
                                .id(Math.toIntExact(entity.getId()))
                                .icNumber(entity.getIcNumber())
                                .poNo(entity.getPoNo())
                                .poSerialNo(entity.getPoSerialNo())
                                .typeOfCall(entity.getTypeOfCall())
                                .ercType(entity.getErcType())
                                .status(entity.getStatus())
                                .desiredInspectionDate(String.valueOf(entity.getDesiredInspectionDate()))
                                .actualInspectionDate(String.valueOf(entity.getActualInspectionDate()))
                                .placeOfInspection(entity.getPlaceOfInspection())
                                .companyId(entity.getCompanyId())
                                .companyName(entity.getCompanyName())
                                .unitId(entity.getUnitId())
                                .unitName(entity.getUnitName())
                                .unitAddress(entity.getUnitAddress())
                                .remarks(entity.getRemarks())
                                .createdBy(entity.getCreatedBy())
                                .updatedBy(entity.getUpdatedBy())
                                .createdAt(String.valueOf(entity.getCreatedAt()))
                                .updatedAt(String.valueOf(entity.getUpdatedAt()))
                                .build();

                // Add RM inspection details if available
                if (entity.getRmInspectionDetails() != null) {
                        dto.setRmInspectionDetails(mapToDetailsDto(entity.getRmInspectionDetails()));
                }

                return dto;
        }

        /**
         * Maps InspectionCall entity to DTO with all related data
         */
        private InspectionCallDto mapToCallDtoWithDetails(InspectionCall entity) {
                InspectionCallDto dto = mapToCallDto(entity);

                // Add RM details and heat quantities if available
                if (entity.getRmInspectionDetails() != null) {
                        RmInspectionDetails rmDetails = entity.getRmInspectionDetails();
                        if (rmDetails.getHeatQuantities() != null) {
                                List<RmHeatQuantityDto> heatDtos = rmDetails.getHeatQuantities()
                                                .stream()
                                                .map(heat -> mapToHeatDtoWithChem(heat, rmDetails.getChemicalAnalysisList()))
                                                .collect(Collectors.toList());
                                dto.setRmHeatQuantities(heatDtos);
                        }
                }

                return dto;
        }

        /**
         * Maps RmInspectionDetails entity to DTO
         */
        private RmInspectionDetailsDto mapToDetailsDto(RmInspectionDetails entity) {
                return RmInspectionDetailsDto.builder()
                                .id(Math.toIntExact(entity.getId()))
                                .icId(Math.toIntExact(
                                                entity.getInspectionCall() != null ? entity.getInspectionCall().getId()
                                                                : null))
                                .itemDescription(entity.getItemDescription())
                                .itemQuantity(entity.getItemQuantity() != null ? entity.getItemQuantity().toString()
                                                : null)
                                .consigneeZonalRailway(entity.getConsigneeZonalRailway())
                                .heatNumbers(entity.getHeatNumbers())
                                .tcNumber(entity.getTcNumber())
                                .tcDate(String.valueOf(entity.getTcDate()))
                                .tcQuantity(entity.getTcQuantity() != null ? entity.getTcQuantity().toString() : null)
                                .manufacturer(entity.getManufacturer())
                                .supplierName(entity.getSupplierName())
                                .supplierAddress(entity.getSupplierAddress())
                                .invoiceNumber(entity.getInvoiceNumber())
                                .invoiceDate(String.valueOf(entity.getInvoiceDate()))
                                .subPoNumber(entity.getSubPoNumber())
                                .subPoDate(String.valueOf(entity.getSubPoDate()))
                                .subPoQty(String.valueOf(entity.getSubPoQty()))
                                .totalOfferedQtyMt(
                                                entity.getTotalOfferedQtyMt() != null
                                                                ? entity.getTotalOfferedQtyMt().toString()
                                                                : null)
                                .offeredQtyErc(entity.getOfferedQtyErc() != null ? entity.getOfferedQtyErc().toString()
                                                : null)
                                .unitOfMeasurement(entity.getUnitOfMeasurement())
                                .rateOfMaterial(String.valueOf(entity.getRateOfMaterial()))
                                .rateOfGst(String.valueOf(entity.getRateOfGst()))
                                .baseValuePo(String.valueOf(entity.getBaseValuePo()))
                                .totalPo(String.valueOf(entity.getTotalPo()))
                                .createdAt(String.valueOf(entity.getCreatedAt()))
                                .updatedAt(String.valueOf(entity.getUpdatedAt()))
                                .build();
        }

        /**
         * Maps RmHeatQuantity entity to DTO
         */
        private RmHeatQuantityDto mapToHeatDto(RmHeatQuantity entity) {
                return RmHeatQuantityDto.builder()
                                .id(Math.toIntExact(entity.getId()))
                                .rmDetailId(Math.toIntExact(
                                                entity.getRmInspectionDetails() != null
                                                                ? entity.getRmInspectionDetails().getId()
                                                                : null))
                                .heatNumber(entity.getHeatNumber())
                                .manufacturer(entity.getManufacturer())
                                .offeredQty(entity.getOfferedQty() != null ? entity.getOfferedQty().doubleValue() : null)
                                .tcNumber(entity.getTcNumber())
                                .tcDate(String.valueOf(entity.getTcDate()))
                                .tcQuantity(entity.getTcQuantity() != null ? entity.getTcQuantity().doubleValue() : null)
                                .qtyLeft(String.valueOf(entity.getQtyLeft()))
                                .qtyAccepted(String.valueOf(entity.getQtyAccepted()))
                                .qtyRejected(String.valueOf(entity.getQtyRejected()))
                                .rejectionReason(entity.getRejectionReason())
                                .createdAt(String.valueOf(entity.getCreatedAt()))
                                .updatedAt(String.valueOf(entity.getUpdatedAt()))
                                .build();
        }

        /**
         * Maps RmHeatQuantity entity to DTO with final result data from
         * rm_heat_final_result table
         */
        private RmHeatQuantityDto mapToHeatDtoWithFinalResult(RmHeatQuantity entity, String icNumber) {
                // Start with basic mapping
                RmHeatQuantityDto dto = RmHeatQuantityDto.builder()
                                .id(Math.toIntExact(entity.getId()))
                                .rmDetailId(Math.toIntExact(
                                                entity.getRmInspectionDetails() != null
                                                                ? entity.getRmInspectionDetails().getId()
                                                                : null))
                                .heatNumber(entity.getHeatNumber())
                                .manufacturer(entity.getManufacturer())
                                .offeredQty(entity.getOfferedQty() != null ? entity.getOfferedQty().doubleValue() : null)
                                .tcNumber(entity.getTcNumber())
                                .tcDate(String.valueOf(entity.getTcDate()))
                                .tcQuantity(entity.getTcQuantity() != null ? entity.getTcQuantity().doubleValue() : null)
                                .qtyLeft(String.valueOf(entity.getQtyLeft()))
                                .qtyAccepted(String.valueOf(entity.getQtyAccepted()))
                                .qtyRejected(String.valueOf(entity.getQtyRejected()))
                                .rejectionReason(entity.getRejectionReason())
                                .createdAt(String.valueOf(entity.getCreatedAt()))
                                .updatedAt(String.valueOf(entity.getUpdatedAt()))
                                .build();

                // Fetch final result data from rm_heat_final_result table
                List<RmHeatFinalResult> finalResults = rmHeatFinalResultRepository
                                .findByInspectionCallNoAndHeatNo(icNumber, entity.getHeatNumber());

                if (!finalResults.isEmpty()) {
                        RmHeatFinalResult finalResult = finalResults.get(0);
                        // Set weight_accepted_mt and weight_offered_mt from final result
                        dto.setWeightAcceptedMt(
                                        finalResult.getWeightAcceptedMt() != null
                                                        ? finalResult.getWeightAcceptedMt().doubleValue()
                                                        : null);
                        dto.setWeightOfferedMt(
                                        finalResult.getWeightOfferedMt() != null
                                                        ? finalResult.getWeightOfferedMt().doubleValue()
                                                        : null);

                        logger.debug("Enriched heat {} with final result: accepted={} MT, offered={} MT",
                                        entity.getHeatNumber(), dto.getWeightAcceptedMt(), dto.getWeightOfferedMt());
                } else {
                        logger.debug("No final result found for heat {} in IC {}", entity.getHeatNumber(), icNumber);
                }

                return dto;
        }

        private RmChemicalAnalysisDto mapToChemicalDto(RmChemicalAnalysis entity) {
                RmChemicalAnalysisDto dto = new RmChemicalAnalysisDto();
                dto.setHeatNumber(entity.getHeatNumber());
                dto.setCarbon(entity.getCarbon());
                dto.setManganese(entity.getManganese());
                dto.setSilicon(entity.getSilicon());
                dto.setSulphur(entity.getSulphur());
                dto.setPhosphorus(entity.getPhosphorus());
                dto.setChromium(entity.getChromium());
                return dto;
        }

        private RmHeatQuantityDto mapToHeatDtoWithChem(RmHeatQuantity entity, List<RmChemicalAnalysis> chemList) {
                RmHeatQuantityDto dto = mapToHeatDto(entity);
                if (chemList != null && entity.getHeatNumber() != null) {
                        List<RmChemicalAnalysisDto> chemDtos = chemList.stream()
                                        .filter(c -> entity.getHeatNumber().equalsIgnoreCase(c.getHeatNumber()))
                                        .map(this::mapToChemicalDto)
                                        .collect(Collectors.toList());
                        dto.setChemicalAnalyses(chemDtos);
                }
                return dto;
        }

        /**
         * Creates a BusinessException for resource not found
         */
        private BusinessException createNotFoundException(String message) {
                return new BusinessException(new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_RESOURCE,
                                message));
        }
}
