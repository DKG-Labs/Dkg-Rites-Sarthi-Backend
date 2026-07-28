package com.sarthi.service.Impl;

import com.sarthi.dto.IcDtos.*;

import com.sarthi.entity.rawmaterial.*;
import com.sarthi.repository.InspectionModificationHistoryRepository;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.service.InspectionCallService;
import com.sarthi.service.InventoryEntryService;
import com.sarthi.util.IcNumberGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Field;
import java.util.Objects;

@Service
@Transactional
public class InspectionCallServiceImpl implements InspectionCallService {

    private static final Logger logger = LoggerFactory.getLogger(InspectionCallServiceImpl.class);

    private final InspectionCallRepository inspectionCallRepository;
    private final IcNumberGenerator icNumberGenerator;
    private final InventoryEntryService inventoryEntryService;

    private final InspectionModificationHistoryRepository modificationHistoryRepository;
    private final com.sarthi.repository.rawmaterial.RmHeatQuantityRepository heatQuantityRepository;
    private final com.sarthi.repository.rawmaterial.RmChemicalAnalysisRepository rmChemicalAnalysisRepository;
    private final com.sarthi.service.WorkflowService workflowService;

    @Autowired
    public InspectionCallServiceImpl(
            InspectionCallRepository inspectionCallRepository,
            IcNumberGenerator icNumberGenerator,
            InventoryEntryService inventoryEntryService,
            InspectionModificationHistoryRepository modificationHistoryRepository,
            com.sarthi.repository.rawmaterial.RmHeatQuantityRepository heatQuantityRepository,
            com.sarthi.repository.rawmaterial.RmChemicalAnalysisRepository rmChemicalAnalysisRepository,
            com.sarthi.service.WorkflowService workflowService) {
        this.inspectionCallRepository = inspectionCallRepository;
        this.icNumberGenerator = icNumberGenerator;
        this.inventoryEntryService = inventoryEntryService;
        this.modificationHistoryRepository = modificationHistoryRepository;
        this.heatQuantityRepository = heatQuantityRepository;
        this.rmChemicalAnalysisRepository = rmChemicalAnalysisRepository;
        this.workflowService = workflowService;
    }

    @Override
    public InspectionCall createInspectionCall(
            InspectionCallRequestDto icRequest,
            RmInspectionDetailsRequestDto rmRequest) {
        logger.info("========== CREATE RAW MATERIAL INSPECTION CALL ==========");
        logger.info("IC Request: {}", icRequest);
        logger.info("RM Details: {}", rmRequest);

        InspectionCall inspectionCall = new InspectionCall();

        // Generate IC Number with daily sequence reset
        LocalDate today = LocalDate.now();
        long dailySequence = inspectionCallRepository.countByTypeOfCallAndCreatedDate("Raw Material", today) + 1;
        String icNumber = icNumberGenerator.generateIcNumber("Raw Material", dailySequence);
        logger.info("Generated IC Number: {} (Daily Sequence: {})", icNumber, dailySequence);

        inspectionCall.setIcNumber(icNumber);
        inspectionCall.setPoNo(icRequest.getPoNo());
        inspectionCall.setPoSerialNo(icRequest.getPoSerialNo());
        inspectionCall.setTypeOfCall(icRequest.getTypeOfCall());
        inspectionCall.setErcType(icRequest.getErcType());
        inspectionCall.setStatus(icRequest.getStatus());
        inspectionCall.setVendorId(icRequest.getVendorId());
        inspectionCall.setPlaceOfInspection(icRequest.getPlaceOfInspection());

        inspectionCall.setDesiredInspectionDate(
                LocalDate.parse(icRequest.getDesiredInspectionDate()));

        if (icRequest.getActualInspectionDate() != null) {
            inspectionCall.setActualInspectionDate(
                    LocalDate.parse(icRequest.getActualInspectionDate()));
        }

        inspectionCall.setCompanyId(icRequest.getCompanyId());
        inspectionCall.setCompanyName(icRequest.getCompanyName());
        inspectionCall.setUnitId(icRequest.getUnitId());
        inspectionCall.setUnitName(icRequest.getUnitName());
        inspectionCall.setUnitAddress(icRequest.getUnitAddress());
        inspectionCall.setRemarks(icRequest.getRemarks());

        inspectionCall.setCreatedBy(icRequest.getCreatedBy());
        inspectionCall.setUpdatedBy(icRequest.getUpdatedBy());
        inspectionCall.setCreatedAt(LocalDateTime.now());
        inspectionCall.setUpdatedAt(LocalDateTime.now());

        // ================== RM INSPECTION DETAILS ==================
        RmInspectionDetails rmDetails = new RmInspectionDetails();
        rmDetails.setInspectionCall(inspectionCall);
        inspectionCall.setRmInspectionDetails(rmDetails);

        rmDetails.setItemDescription(rmRequest.getItemDescription());
        rmDetails.setItemQuantity(rmRequest.getItemQuantity());
        rmDetails.setConsigneeZonalRailway(rmRequest.getConsigneeZonalRailway());
        rmDetails.setHeatNumbers(rmRequest.getHeatNumbers());
        rmDetails.setTcNumber(rmRequest.getTcNumber());

        if (rmRequest.getTcDate() != null) {
            rmDetails.setTcDate(LocalDate.parse(rmRequest.getTcDate()));
        }

        rmDetails.setTcQuantity(toBigDecimal(rmRequest.getTcQuantity()));
        rmDetails.setManufacturer(rmRequest.getManufacturer());
        rmDetails.setSupplierName(rmRequest.getSupplierName());
        rmDetails.setSupplierAddress(rmRequest.getSupplierAddress());

        rmDetails.setInvoiceNumber(rmRequest.getInvoiceNumber());
        if (rmRequest.getInvoiceDate() != null) {
            rmDetails.setInvoiceDate(LocalDate.parse(rmRequest.getInvoiceDate()));
        }

        rmDetails.setSubPoNumber(rmRequest.getSubPoNumber());
        if (rmRequest.getSubPoDate() != null) {
            rmDetails.setSubPoDate(LocalDate.parse(rmRequest.getSubPoDate()));
        }

        rmDetails.setSubPoQty(rmRequest.getSubPoQty());
        rmDetails.setTotalOfferedQtyMt(toBigDecimal(rmRequest.getTotalOfferedQtyMt()));
        rmDetails.setOfferedQtyErc(rmRequest.getOfferedQtyErc());
        rmDetails.setUnitOfMeasurement(rmRequest.getUnitOfMeasurement());

        rmDetails.setRateOfMaterial(toBigDecimal(rmRequest.getRateOfMaterial()));
        rmDetails.setRateOfGst(toBigDecimal(rmRequest.getRateOfGst()));
        rmDetails.setBaseValuePo(toBigDecimal(rmRequest.getBaseValuePo()));
        rmDetails.setTotalPo(toBigDecimal(rmRequest.getTotalPo()));

        rmDetails.setCreatedAt(LocalDateTime.now());
        rmDetails.setUpdatedAt(LocalDateTime.now());

        // ================== HEAT QUANTITIES ==================
        List<RmHeatQuantity> heatEntities = new ArrayList<>();

        if (rmRequest.getHeatQuantities() != null) {
            for (RmHeatQuantityRequestDto heatReq : rmRequest.getHeatQuantities()) {

                RmHeatQuantity heat = new RmHeatQuantity();
                heat.setRmInspectionDetails(rmDetails);

                heat.setHeatNumber(heatReq.getHeatNumber());
                heat.setManufacturer(heatReq.getManufacturer());
                heat.setOfferedQty(toBigDecimal(heatReq.getOfferedQty()));

                heat.setTcNumber(heatReq.getTcNumber());
                if (heatReq.getTcDate() != null) {
                    heat.setTcDate(LocalDate.parse(heatReq.getTcDate()));
                }

                heat.setTcQuantity(toBigDecimal(heatReq.getTcQuantity()));
                heat.setQtyLeft(toBigDecimal(heatReq.getQtyLeft()));
                heat.setQtyAccepted(toBigDecimal(heatReq.getQtyAccepted()));
                heat.setQtyRejected(toBigDecimal(heatReq.getQtyRejected()));
                heat.setRejectionReason(heatReq.getRejectionReason());

                heat.setCreatedAt(LocalDateTime.now());
                heat.setUpdatedAt(LocalDateTime.now());

                heatEntities.add(heat);

                // ================== UPDATE INVENTORY ==================
                // Update inventory offered quantity for this heat/TC combination
                // This joins the main transaction so it rolls back if workflow fails.
                try {
                    if (heatReq.getHeatNumber() != null && heatReq.getTcNumber() != null
                            && heatReq.getOfferedQty() != null) {
                        
                        String subPoNo = rmRequest.getSubPoNumber();
                        com.sarthi.dto.InventoryEntryResponseDto existingEntry = inventoryEntryService.getInventoryEntryByHeatAndTcAndSubPo(heatReq.getHeatNumber(), heatReq.getTcNumber(), subPoNo);
                        
                        if (existingEntry != null) {
                            logger.info("Updating inventory for Heat: {}, TC: {}, Sub PO: {}, Offered: {}",
                                    heatReq.getHeatNumber(), heatReq.getTcNumber(), subPoNo, heatReq.getOfferedQty());

                            inventoryEntryService.updateOfferedQuantity(
                                    heatReq.getHeatNumber(),
                                    heatReq.getTcNumber(),
                                    subPoNo,
                                    toBigDecimal(heatReq.getOfferedQty()));

                            logger.info("✅ Inventory updated successfully for Heat: {}", heatReq.getHeatNumber());
                        } else {
                            logger.warn("⚠️ Inventory entry not found for Heat: {}, TC: {}, Sub PO: {}. Skipping inventory update. Inspection call will still be created.", heatReq.getHeatNumber(), heatReq.getTcNumber(), subPoNo);
                        }
                    }
                } catch (Exception e) {
                    // Log error but don't fail the inspection call creation
                    logger.warn("⚠️ Failed to update inventory for Heat: {}, TC: {}. Error: {}",
                            heatReq.getHeatNumber(), heatReq.getTcNumber(), e.getMessage());
                }
            }
        }

        rmDetails.setHeatQuantities(heatEntities);

        // ================== CHEMICAL ANALYSIS ==================
        List<RmChemicalAnalysis> chemEntities = new ArrayList<>();

        if (rmRequest.getChemicalAnalysis() != null) {
            for (RmChemicalAnalysisRequestDto chemReq : rmRequest.getChemicalAnalysis()) {

                RmChemicalAnalysis chem = new RmChemicalAnalysis();
                chem.setRmInspectionDetails(rmDetails);

                chem.setHeatNumber(chemReq.getHeatNumber());
                chem.setCarbon(toBigDecimal(chemReq.getCarbon()));
                chem.setManganese(toBigDecimal(chemReq.getManganese()));
                chem.setSilicon(toBigDecimal(chemReq.getSilicon()));
                chem.setSulphur(toBigDecimal(chemReq.getSulphur()));
                chem.setPhosphorus(toBigDecimal(chemReq.getPhosphorus()));
                chem.setChromium(toBigDecimal(chemReq.getChromium()));

                chem.setCreatedAt(LocalDateTime.now());
                chem.setUpdatedAt(LocalDateTime.now());

                chemEntities.add(chem);
            }
        }

        rmDetails.setChemicalAnalysisList(chemEntities);

        InspectionCall savedIc = inspectionCallRepository.save(inspectionCall);

        // Trigger workflow ONLY on success of save, but inside the same transaction
        // so if workflow fails, the save is rolled back.
        String workflowName = "INSPECTION CALL";
        Integer createdByUserId = null;
        try {
            createdByUserId = Integer.valueOf(savedIc.getCreatedBy());
        } catch (NumberFormatException e) {
            logger.warn("⚠️ createdBy is not a valid integer: {}. Skipping workflow initiation.", savedIc.getCreatedBy());
        }

        if (createdByUserId != null) {
            workflowService.initiateWorkflow(
                    savedIc.getIcNumber(),
                    createdByUserId,
                    workflowName,
                    "560001"
            );
            logger.info("✅ Workflow initiated for IC: {}", savedIc.getIcNumber());
        }

        return savedIc;
    }

    private BigDecimal toBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }

    @Override
    public boolean checkIfCallExistsForPoSerial(String poSerialNo) {
        logger.info("Checking if inspection call exists for PO Serial No: {}", poSerialNo);
        boolean exists = inspectionCallRepository.existsByPoSerialNo(poSerialNo);
        logger.info("Inspection call exists for PO Serial No {}: {}", poSerialNo, exists);
        return exists;
    }


    @Override
    @Transactional
    public InspectionCall modifyInspectionCall(
            String icNumber,
            InspectionCallRequestDto icDto,
            RmInspectionDetailsRequestDto rmDto) {

        InspectionCall inspection =
                inspectionCallRepository
                        .findByIcNumber(icNumber)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Inspection Call Not Found"));

        RmInspectionDetails rmDetails =
                inspection.getRmInspectionDetails();

        // =====================================================
        // PROCESS INSPECTION CALL DTO
        // =====================================================

        processDtoFields(
                icDto,
                inspection,
                inspection,
                "inspection_call",
                1,
                icDto.getUpdatedBy());

        // =====================================================
        // PROCESS RM DETAILS DTO
        // =====================================================

        processDtoFields(
                rmDto,
                rmDetails,
                inspection,
                "rm_inspection_details",
                1,
                icDto.getUpdatedBy());

        // =====================================================
        // PROCESS HEAT QUANTITIES
        // =====================================================
        if (rmDto.getHeatQuantities() != null) {
            List<RmHeatQuantity> existingHeats = heatQuantityRepository.findByRmDetailId(Math.toIntExact(rmDetails.getId()));

            // 1. Delete heats no longer present in the request (matched by heatNumber)
            for (RmHeatQuantity existing : existingHeats) {
                boolean stillExists = rmDto.getHeatQuantities().stream()
                        .anyMatch(dto -> dto.getHeatNumber() != null && dto.getHeatNumber().equalsIgnoreCase(existing.getHeatNumber()));
                if (!stillExists) {
                    // Reinstate quantity to inventory since this heat is removed from the call
                    BigDecimal oldQty = existing.getOfferedQty();
                    if (oldQty != null && oldQty.compareTo(BigDecimal.ZERO) > 0 && existing.getHeatNumber() != null && existing.getTcNumber() != null) {
                        try {
                            logger.info("Reinstating inventory for deleted heat: {}, TC: {}, Qty: {}", existing.getHeatNumber(), existing.getTcNumber(), oldQty);
                            inventoryEntryService.updateOfferedQuantity(existing.getHeatNumber(), existing.getTcNumber(), oldQty.negate());
                        } catch (Exception e) {
                            logger.error("Failed to reinstate inventory for deleted heat: " + existing.getHeatNumber() + ", TC: " + existing.getTcNumber(), e);
                        }
                    }
                    heatQuantityRepository.delete(existing);
                }
            }

            // 2. Update existing heats or insert new ones
            for (RmHeatQuantityRequestDto dto : rmDto.getHeatQuantities()) {
                if (dto.getHeatNumber() == null || dto.getHeatNumber().trim().isEmpty()) {
                    continue;
                }

                RmHeatQuantity heat = existingHeats.stream()
                        .filter(existing -> dto.getHeatNumber().equalsIgnoreCase(existing.getHeatNumber()))
                        .findFirst()
                        .orElse(null);

                if (heat != null) {
                    // Inventory Adjustment: Compare old and new values
                    String oldTc = heat.getTcNumber();
                    BigDecimal oldQty = heat.getOfferedQty() != null ? heat.getOfferedQty() : BigDecimal.ZERO;
                    String newTc = dto.getTcNumber();
                    BigDecimal newQty = toBigDecimal(dto.getOfferedQty()) != null ? toBigDecimal(dto.getOfferedQty()) : BigDecimal.ZERO;

                    if (oldTc != null && newTc != null) {
                        if (oldTc.equalsIgnoreCase(newTc)) {
                            // Sub-case 1: TC number is the same, quantity might have changed
                            BigDecimal difference = newQty.subtract(oldQty);
                            if (difference.compareTo(BigDecimal.ZERO) != 0) {
                                try {
                                    logger.info("Adjusting inventory for heat: {}, TC: {}, difference: {}", heat.getHeatNumber(), newTc, difference);
                                    inventoryEntryService.updateOfferedQuantity(heat.getHeatNumber(), newTc, difference);
                                } catch (Exception e) {
                                    logger.error("Failed to adjust inventory for heat: " + heat.getHeatNumber() + ", TC: " + newTc, e);
                                }
                            }
                        } else {
                            // Sub-case 2: TC number changed
                            // Reinstate old quantity to old TC
                            if (oldQty.compareTo(BigDecimal.ZERO) > 0) {
                                try {
                                    logger.info("Reinstating old TC inventory: Heat: {}, TC: {}, Qty: {}", heat.getHeatNumber(), oldTc, oldQty);
                                    inventoryEntryService.updateOfferedQuantity(heat.getHeatNumber(), oldTc, oldQty.negate());
                                } catch (Exception e) {
                                    logger.error("Failed to reinstate old TC inventory for heat: " + heat.getHeatNumber() + ", TC: " + oldTc, e);
                                }
                            }
                            // Deduct new quantity from new TC
                            if (newQty.compareTo(BigDecimal.ZERO) > 0) {
                                try {
                                    logger.info("Deducting new TC inventory: Heat: {}, TC: {}, Qty: {}", heat.getHeatNumber(), newTc, newQty);
                                    inventoryEntryService.updateOfferedQuantity(heat.getHeatNumber(), newTc, newQty);
                                } catch (Exception e) {
                                    logger.error("Failed to deduct new TC inventory for heat: " + heat.getHeatNumber() + ", TC: " + newTc, e);
                                }
                            }
                        }
                    } else if (newTc != null && newQty.compareTo(BigDecimal.ZERO) > 0) {
                        // If oldTc was null, just deduct the new quantity from the new TC
                        try {
                            logger.info("Deducting inventory for heat: {}, TC: {}, Qty: {}", heat.getHeatNumber(), newTc, newQty);
                            inventoryEntryService.updateOfferedQuantity(heat.getHeatNumber(), newTc, newQty);
                        } catch (Exception e) {
                            logger.error("Failed to deduct inventory for heat: " + heat.getHeatNumber() + ", TC: " + newTc, e);
                        }
                    }

                    // Update existing RmHeatQuantity record
                    heat.setManufacturer(dto.getManufacturer());
                    heat.setOfferedQty(toBigDecimal(dto.getOfferedQty()));
                    heat.setTcNumber(dto.getTcNumber());
                    if (dto.getTcDate() != null && !dto.getTcDate().trim().isEmpty()) {
                        heat.setTcDate(LocalDate.parse(dto.getTcDate()));
                    } else {
                        heat.setTcDate(null);
                    }
                    heat.setTcQuantity(toBigDecimal(dto.getTcQuantity()));
                    heat.setQtyLeft(toBigDecimal(dto.getQtyLeft()));
                    heat.setQtyAccepted(toBigDecimal(dto.getQtyAccepted()));
                    heat.setQtyRejected(toBigDecimal(dto.getQtyRejected()));
                    heat.setRejectionReason(dto.getRejectionReason());
                    heat.setUpdatedAt(LocalDateTime.now());
                    heatQuantityRepository.save(heat);
                } else {
                    // Heat is brand new: deduct new quantity from new TC
                    BigDecimal newQty = toBigDecimal(dto.getOfferedQty()) != null ? toBigDecimal(dto.getOfferedQty()) : BigDecimal.ZERO;
                    if (dto.getTcNumber() != null && newQty.compareTo(BigDecimal.ZERO) > 0) {
                        try {
                            logger.info("Deducting inventory for new heat: {}, TC: {}, Qty: {}", dto.getHeatNumber(), dto.getTcNumber(), newQty);
                            inventoryEntryService.updateOfferedQuantity(dto.getHeatNumber(), dto.getTcNumber(), newQty);
                        } catch (Exception e) {
                            logger.error("Failed to deduct inventory for new heat: " + dto.getHeatNumber() + ", TC: " + dto.getTcNumber(), e);
                        }
                    }

                    // Insert new RmHeatQuantity record
                    RmHeatQuantity newHeat = new RmHeatQuantity();
                    newHeat.setRmInspectionDetails(rmDetails);
                    newHeat.setHeatNumber(dto.getHeatNumber());
                    newHeat.setManufacturer(dto.getManufacturer());
                    newHeat.setOfferedQty(toBigDecimal(dto.getOfferedQty()));
                    newHeat.setTcNumber(dto.getTcNumber());
                    if (dto.getTcDate() != null && !dto.getTcDate().trim().isEmpty()) {
                        newHeat.setTcDate(LocalDate.parse(dto.getTcDate()));
                    }
                    newHeat.setTcQuantity(toBigDecimal(dto.getTcQuantity()));
                    newHeat.setQtyLeft(toBigDecimal(dto.getQtyLeft()));
                    newHeat.setQtyAccepted(toBigDecimal(dto.getQtyAccepted()));
                    newHeat.setQtyRejected(toBigDecimal(dto.getQtyRejected()));
                    newHeat.setRejectionReason(dto.getRejectionReason());
                    newHeat.setCreatedAt(LocalDateTime.now());
                    newHeat.setUpdatedAt(LocalDateTime.now());
                    heatQuantityRepository.save(newHeat);
                }
            }
        }

        // =====================================================
        // PROCESS CHEMICAL ANALYSIS
        // =====================================================
        if (rmDto.getChemicalAnalysis() != null) {
            List<RmChemicalAnalysis> existingChems = rmChemicalAnalysisRepository.findByRmInspectionDetailsId(Math.toIntExact(rmDetails.getId()));

            // 1. Delete chemical analyses no longer present in the request (matched by heatNumber)
            for (RmChemicalAnalysis existing : existingChems) {
                boolean stillExists = rmDto.getChemicalAnalysis().stream()
                        .anyMatch(dto -> dto.getHeatNumber() != null && dto.getHeatNumber().equalsIgnoreCase(existing.getHeatNumber()));
                if (!stillExists) {
                    rmChemicalAnalysisRepository.delete(existing);
                }
            }

            // 2. Update existing or insert new ones
            for (RmChemicalAnalysisRequestDto dto : rmDto.getChemicalAnalysis()) {
                if (dto.getHeatNumber() == null || dto.getHeatNumber().trim().isEmpty()) {
                    continue;
                }

                RmChemicalAnalysis chem = existingChems.stream()
                        .filter(existing -> dto.getHeatNumber().equalsIgnoreCase(existing.getHeatNumber()))
                        .findFirst()
                        .orElse(null);

                if (chem != null) {
                    // Update existing
                    chem.setCarbon(toBigDecimal(dto.getCarbon()));
                    chem.setManganese(toBigDecimal(dto.getManganese()));
                    chem.setSilicon(toBigDecimal(dto.getSilicon()));
                    chem.setSulphur(toBigDecimal(dto.getSulphur()));
                    chem.setPhosphorus(toBigDecimal(dto.getPhosphorus()));
                    chem.setChromium(toBigDecimal(dto.getChromium()));
                    chem.setUpdatedAt(LocalDateTime.now());
                    rmChemicalAnalysisRepository.save(chem);
                } else {
                    // Insert new
                    RmChemicalAnalysis newChem = new RmChemicalAnalysis();
                    newChem.setRmInspectionDetails(rmDetails);
                    newChem.setHeatNumber(dto.getHeatNumber());
                    newChem.setCarbon(toBigDecimal(dto.getCarbon()));
                    newChem.setManganese(toBigDecimal(dto.getManganese()));
                    newChem.setSilicon(toBigDecimal(dto.getSilicon()));
                    newChem.setSulphur(toBigDecimal(dto.getSulphur()));
                    newChem.setPhosphorus(toBigDecimal(dto.getPhosphorus()));
                    newChem.setChromium(toBigDecimal(dto.getChromium()));
                    newChem.setCreatedAt(LocalDateTime.now());
                    newChem.setUpdatedAt(LocalDateTime.now());
                    rmChemicalAnalysisRepository.save(newChem);
                }
            }
        }

        // =====================================================
        // FINAL UPDATE
        // =====================================================

        inspection.setIsModified(true);

        inspection.setUpdatedBy(icDto.getUpdatedBy());

        inspection.setUpdatedAt(
                LocalDateTime.now());

        return inspectionCallRepository.save(inspection);
    }

    @Override
    public void processDtoFields(
            Object dto,
            Object entity,
            InspectionCall inspection,
            String tableName,
            Integer modificationVersion,
            String modifiedBy) {

        if (dto == null || entity == null) {
            return;
        }

        Field[] dtoFields =
                dto.getClass().getDeclaredFields();

        for (Field dtoField : dtoFields) {

            try {

                if (java.util.Collection.class.isAssignableFrom(dtoField.getType()) ||
                    java.util.Map.class.isAssignableFrom(dtoField.getType())) {
                    continue;
                }

                dtoField.setAccessible(true);

                Object newValue =
                        dtoField.get(dto);

                // skip null fields
                if (newValue == null) {
                    continue;
                }

                String fieldName =
                        dtoField.getName();

                Field entityField =
                        entity.getClass()
                                .getDeclaredField(fieldName);

                entityField.setAccessible(true);

                Object oldValue =
                        entityField.get(entity);

                // skip same values
                if (Objects.equals(
                        String.valueOf(oldValue),
                        String.valueOf(newValue))) {
                    continue;
                }

                // =================================================
                // UPDATE ENTITY
                // =================================================

                Class<?> targetType = entityField.getType();
                Object convertedValue = newValue;

                if (newValue != null) {
                    if (targetType.equals(LocalDate.class) && newValue instanceof String) {
                        convertedValue = LocalDate.parse((String) newValue);
                    } else if (targetType.equals(LocalDateTime.class) && newValue instanceof String) {
                        convertedValue = LocalDateTime.parse((String) newValue);
                    } else if (targetType.equals(BigDecimal.class)) {
                        if (newValue instanceof Double) {
                            convertedValue = BigDecimal.valueOf((Double) newValue);
                        } else if (newValue instanceof Integer) {
                            convertedValue = BigDecimal.valueOf((Integer) newValue);
                        } else if (newValue instanceof String) {
                            convertedValue = new BigDecimal((String) newValue);
                        }
                    } else if (targetType.equals(Long.class) || targetType.equals(long.class)) {
                        if (newValue instanceof Integer) {
                            convertedValue = ((Integer) newValue).longValue();
                        } else if (newValue instanceof String) {
                            convertedValue = Long.parseLong((String) newValue);
                        }
                    } else if (targetType.equals(Double.class) || targetType.equals(double.class)) {
                        if (newValue instanceof Integer) {
                            convertedValue = ((Integer) newValue).doubleValue();
                        } else if (newValue instanceof String) {
                            convertedValue = Double.parseDouble((String) newValue);
                        }
                    } else if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
                        if (newValue instanceof String) {
                            convertedValue = Integer.parseInt((String) newValue);
                        }
                    }
                }

                entityField.set(entity, convertedValue);

                // =================================================
                // SAVE MODIFICATION HISTORY
                // =================================================

                saveModificationHistory(
                        inspection,
                        modificationVersion,
                        tableName,
                        fieldName,
                        oldValue,
                        newValue,
                        modifiedBy);

                logger.info(
                        "Field Updated :: {} -> {}",
                        fieldName,
                        newValue);

            } catch (NoSuchFieldException e) {

                logger.warn(
                        "Field Not Found In Entity :: {}",
                        dtoField.getName());

            } catch (Exception e) {

                logger.error(
                        "Error Updating Field :: {}",
                        dtoField.getName(),
                        e);
            }
        }
    }

    @Override
    public void saveModificationHistory(
            InspectionCall inspection,
            Integer modificationVersion,
            String tableName,
            String fieldName,
            Object oldValue,
            Object newValue,
            String modifiedBy) {

        InspectionModificationHistory history =
                new InspectionModificationHistory();

        history.setInspectionCallId(
                inspection.getId());

        history.setIcNumber(
                inspection.getIcNumber());

        history.setModificationVersion(
                modificationVersion);

        history.setTableName(
                tableName);

        history.setFieldName(
                fieldName);

        history.setOldValue(
                oldValue != null
                        ? oldValue.toString()
                        : null);

        history.setNewValue(
                newValue != null
                        ? newValue.toString()
                        : null);

        history.setModifiedBy(
                modifiedBy);

        history.setModifiedAt(
                LocalDateTime.now());

        history.setChangeType("UPDATE");

        modificationHistoryRepository.save(history);
    }
}
