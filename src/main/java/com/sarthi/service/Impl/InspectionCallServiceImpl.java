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

    @Autowired
    public InspectionCallServiceImpl(
            InspectionCallRepository inspectionCallRepository,
            IcNumberGenerator icNumberGenerator,
            InventoryEntryService inventoryEntryService,
            InspectionModificationHistoryRepository modificationHistoryRepository,
            com.sarthi.repository.rawmaterial.RmHeatQuantityRepository heatQuantityRepository,
            com.sarthi.repository.rawmaterial.RmChemicalAnalysisRepository rmChemicalAnalysisRepository) {
        this.inspectionCallRepository = inspectionCallRepository;
        this.icNumberGenerator = icNumberGenerator;
        this.inventoryEntryService = inventoryEntryService;
        this.modificationHistoryRepository = modificationHistoryRepository;
        this.heatQuantityRepository = heatQuantityRepository;
        this.rmChemicalAnalysisRepository = rmChemicalAnalysisRepository;
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
                // This runs in a separate transaction (REQUIRES_NEW) to avoid rollback issues
                try {
                    if (heatReq.getHeatNumber() != null && heatReq.getTcNumber() != null
                            && heatReq.getOfferedQty() != null) {
                        logger.info("Updating inventory for Heat: {}, TC: {}, Offered: {}",
                                heatReq.getHeatNumber(), heatReq.getTcNumber(), heatReq.getOfferedQty());

                        inventoryEntryService.updateOfferedQuantity(
                                heatReq.getHeatNumber(),
                                heatReq.getTcNumber(),
                                toBigDecimal(heatReq.getOfferedQty()));

                        logger.info("✅ Inventory updated successfully for Heat: {}", heatReq.getHeatNumber());
                    }
                } catch (Exception e) {
                    // Log error but don't fail the inspection call creation
                    // Inventory update runs in separate transaction, so this won't affect main
                    // transaction
                    logger.warn("⚠️ Failed to update inventory for Heat: {}, TC: {}. Error: {}",
                            heatReq.getHeatNumber(), heatReq.getTcNumber(), e.getMessage());
                    logger.warn(
                            "⚠️ This is expected if inventory entry doesn't exist yet. Inspection call will still be created.");
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

        return inspectionCallRepository.save(inspectionCall);
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
                    // Update existing
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
                    // Insert new
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

                entityField.set(entity, newValue);

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
