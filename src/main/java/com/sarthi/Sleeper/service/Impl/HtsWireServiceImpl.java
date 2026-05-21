package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.HtsWire.HtsCoilDetailsRequestDto;
import com.sarthi.Sleeper.dto.HtsWire.HtsCoilDetailsResponseDto;
import com.sarthi.Sleeper.dto.HtsWire.HtsWireRequestDto;
import com.sarthi.Sleeper.dto.HtsWire.HtsWireResponseDto;
import com.sarthi.Sleeper.entity.DowelInventory;
import com.sarthi.Sleeper.entity.SleeperWorkflowTransaction;
import com.sarthi.Sleeper.entity.VendorHtsWire.HtsCoilDetails;
import com.sarthi.Sleeper.entity.VendorHtsWire.HtsWire;
import com.sarthi.Sleeper.repository.HtsWireRepository;
import com.sarthi.Sleeper.repository.SleeperWorkflowRepository;
import com.sarthi.Sleeper.service.HtsWireService;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HtsWireServiceImpl implements HtsWireService {

    private final HtsWireRepository repository;
    @Autowired
    private SleeperWorkflowRepository sleeperWorkflowRepository;


    // ================= CREATE =================

    @Override
    public HtsWireResponseDto create(HtsWireRequestDto dto) {

        HtsWire entity = new HtsWire();

        mapBasicFields(entity, dto);

        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());

        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        mapCoilDetails(entity, dto);

        HtsWire saved = repository.save(entity);

        return mapToResponse(saved);
    }


    // ================= UPDATE =================

    @Override
    public HtsWireResponseDto update(Long id, HtsWireRequestDto dto) {

        HtsWire entity = repository.findById(id)
                            .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                        "HTS Wire not found")));

        mapBasicFields(entity, dto);

        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        // Clear old child records
        entity.getCoilDetails().clear();

        mapCoilDetails(entity, dto);

        HtsWire updated = repository.save(entity);

        return mapToResponse(updated);
    }


    // ================= GET BY ID =================

    @Override
    public HtsWireResponseDto getById(Long id) {

        HtsWire entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "HTS Wire not found")));

        return mapToResponse(entity);
    }


    // ================= GET ALL =================

    @Override
    public List<HtsWireResponseDto> getAll() {

        List<HtsWire> entities = repository.findAll();

        Map<String, String> statusMap = sleeperWorkflowRepository
                .findAllLatestStatuses(5L)
                .stream()
                .collect(Collectors.toMap(
                        obj -> String.valueOf(obj[0]),
                        obj -> String.valueOf(obj[1])
                ));

        return entities.stream()
                .map(entity -> mapToResp(entity, statusMap))
                .toList();
    }


    // ================= DELETE =================

    @Override
    public void delete(Long id) {
        HtsWire entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "HTS Wire not found")));

        repository.deleteById(entity.getId());

        Long moduleId = 5L;

        SleeperWorkflowTransaction lastWorkflow =
                sleeperWorkflowRepository
                        .findTopByModuleIdAndRequestIdOrderByWorkflowTransitionIdDesc(
                                moduleId,
                                String.valueOf(entity.getId())
                        );

        SleeperWorkflowTransaction newWorkflow = new SleeperWorkflowTransaction();

        newWorkflow.setModuleId(moduleId);
        newWorkflow.setRequestId(String.valueOf(entity.getId()));

        newWorkflow.setAction("DELETE");
        newWorkflow.setStatus("DELETED");

        if (lastWorkflow != null) {
            newWorkflow.setWorkflowId(lastWorkflow.getWorkflowId());
            newWorkflow.setCurrentRole(lastWorkflow.getCurrentRole());
            newWorkflow.setNextRole(null);
            newWorkflow.setAssignedToUser(lastWorkflow.getAssignedToUser());
        }

        newWorkflow.setModifiedBy(Long.valueOf(entity.getCreatedBy()));
        newWorkflow.setCreatedDate(LocalDateTime.now());

        sleeperWorkflowRepository.save(newWorkflow);
    }


    // ================= BASIC FIELD MAPPER =================

    private void mapBasicFields(HtsWire entity,
                                HtsWireRequestDto dto) {

        entity.setGradeSpec(dto.getGradeSpec());
        entity.setManufacturer(dto.getManufacturer());

        entity.setInvoiceNumber(dto.getInvoiceNumber());
        entity.setRitesIcNumber(dto.getRitesIcNumber());

        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        entity.setRelaxationTest(dto.getRelaxationTest());
        entity.setRelaxationTestTc(dto.getRelaxationTestTc());

        if (dto.getDateOfReceipt() != null) {
            entity.setDateOfReceipt(
                    CommonUtils.convertStringToDateObject(
                            dto.getDateOfReceipt()));
        }

        if (dto.getInvoiceDate() != null) {
            entity.setInvoiceDate(
                    CommonUtils.convertStringToDateObject(
                            dto.getInvoiceDate()));
        }

        if (dto.getRitesIcDate() != null) {
            entity.setRitesIcDate(
                    CommonUtils.convertStringToDateObject(
                            dto.getRitesIcDate()));
        }

        if (dto.getRelaxationTestDate() != null) {
            entity.setRelaxationTestDate(
                    CommonUtils.convertStringToDateObject(
                            dto.getRelaxationTestDate()));
        }

        if (dto.getRelaxationTestValidity() != null && !dto.getRelaxationTestValidity().trim().isEmpty()) {
            entity.setRelaxationTestValidity(
                    CommonUtils.convertStringToDateObject(
                            dto.getRelaxationTestValidity()));
        } else if (entity.getRelaxationTestDate() != null && entity.getRelaxationTest() != null) {
            if ("1000 Hours Test".equalsIgnoreCase(entity.getRelaxationTest())) {
                entity.setRelaxationTestValidity(entity.getRelaxationTestDate().plusYears(1));
            } else if ("100 Hours Test".equalsIgnoreCase(entity.getRelaxationTest())) {
                entity.setRelaxationTestValidity(entity.getRelaxationTestDate().plusMonths(6));
            }
        }
    }


    // ================= COIL MAPPING + TOTAL =================



    private void mapCoilDetails(HtsWire entity,
                                HtsWireRequestDto dto) {

        double totalQty = 0;

        if (dto.getCoilDetails() != null) {

            for (HtsCoilDetailsRequestDto cDto :
                    dto.getCoilDetails()) {

                HtsCoilDetails coil = new HtsCoilDetails();

                coil.setEntryType(cDto.getEntryType());
                coil.setLotNo(cDto.getLotNo());
                coil.setQtyKg(cDto.getQtyKg());


                // 🔥 SINGLE
                if ("SINGLE".equalsIgnoreCase(cDto.getEntryType())) {

                    coil.setCoilNo(cDto.getCoilNo());
                    coil.setCoilFrom(null);
                    coil.setCoilTo(null);
                }

                // 🔥 RANGE
                else if ("RANGE".equalsIgnoreCase(cDto.getEntryType())) {

                    coil.setCoilFrom(cDto.getCoilFrom());
                    coil.setCoilTo(cDto.getCoilTo());
                    coil.setCoilNo(null);
                }

                coil.setHtsWire(entity);

                entity.getCoilDetails().add(coil);

                if (cDto.getQtyKg() != null) {
                    totalQty += cDto.getQtyKg();
                }
            }
        }

        entity.setTotalQtyReceived(totalQty);
    }
    private HtsWireResponseDto mapToResp(HtsWire entity, Map<String, String> statusMap) {

        HtsWireResponseDto dto =
                new HtsWireResponseDto();

        dto.setId(entity.getId());
        dto.setGradeSpec(entity.getGradeSpec());
        dto.setManufacturer(entity.getManufacturer());

        dto.setInvoiceNumber(entity.getInvoiceNumber());
        dto.setRitesIcNumber(entity.getRitesIcNumber());

        dto.setVendorCode(entity.getVendorCode());
        dto.setPlantId(entity.getPlantId());
        // dto.setCreatedBy(entity.getCreatedBy());
        Integer createdBy = entity.getCreatedBy();

        if (createdBy != null) {
            dto.setCreatedBy(createdBy);
        } else {
            dto.setCreatedBy(0);
        }
        dto.setRelaxationTest(entity.getRelaxationTest());
        dto.setRelaxationTestTc(entity.getRelaxationTestTc());
        dto.setTotalQtyReceived(entity.getTotalQtyReceived());

        if (entity.getDateOfReceipt() != null) {
            dto.setDateOfReceipt(
                    CommonUtils.convertDateToString(
                            entity.getDateOfReceipt()));
        }

        if (entity.getInvoiceDate() != null) {
            dto.setInvoiceDate(
                    CommonUtils.convertDateToString(
                            entity.getInvoiceDate()));
        }

        if (entity.getRitesIcDate() != null) {
            dto.setRitesIcDate(
                    CommonUtils.convertDateToString(
                            entity.getRitesIcDate()));
        }

        if (entity.getRelaxationTestDate() != null) {
            dto.setRelaxationTestDate(
                    CommonUtils.convertDateToString(
                            entity.getRelaxationTestDate()));
        }

        if (entity.getRelaxationTestValidity() != null) {
            dto.setRelaxationTestValidity(
                    CommonUtils.convertDateToString(
                            entity.getRelaxationTestValidity()));
        }
        String status = statusMap.getOrDefault(
                String.valueOf(entity.getId()),
                "NOT_STARTED"
        );

        if (status != null) {
            dto.setStatus(status);
        }


        // ===== CHILD COILS =====

        if (entity.getCoilDetails() != null) {

            List<HtsCoilDetailsResponseDto> coilList =
                    entity.getCoilDetails()
                            .stream()
                            .map(c -> {

                                HtsCoilDetailsResponseDto cd =
                                        new HtsCoilDetailsResponseDto();

                                cd.setId(c.getId());
                                cd.setEntryType(c.getEntryType());
                                cd.setLotNo(c.getLotNo());
                                cd.setQtyKg(c.getQtyKg());

                                cd.setCoilNo(c.getCoilNo());
                                cd.setCoilFrom(c.getCoilFrom());
                                cd.setCoilTo(c.getCoilTo());

                                return cd;
                            })
                            .toList();

            dto.setCoilDetails(coilList);
        }


        return dto;
    }


    // ================= RESPONSE MAPPER =================

    private HtsWireResponseDto mapToResponse(HtsWire entity) {

        HtsWireResponseDto dto =
                new HtsWireResponseDto();

        dto.setId(entity.getId());
        dto.setGradeSpec(entity.getGradeSpec());
        dto.setManufacturer(entity.getManufacturer());

        dto.setInvoiceNumber(entity.getInvoiceNumber());
        dto.setRitesIcNumber(entity.getRitesIcNumber());

        dto.setVendorCode(entity.getVendorCode());
        dto.setPlantId(entity.getPlantId());
       // dto.setCreatedBy(entity.getCreatedBy());
        Integer createdBy = entity.getCreatedBy();

        if (createdBy != null) {
            dto.setCreatedBy(createdBy);
        } else {
            dto.setCreatedBy(0);
        }
        dto.setRelaxationTest(entity.getRelaxationTest());
        dto.setRelaxationTestTc(entity.getRelaxationTestTc());
        dto.setTotalQtyReceived(entity.getTotalQtyReceived());

        if (entity.getDateOfReceipt() != null) {
            dto.setDateOfReceipt(
                    CommonUtils.convertDateToString(
                            entity.getDateOfReceipt()));
        }

        if (entity.getInvoiceDate() != null) {
            dto.setInvoiceDate(
                    CommonUtils.convertDateToString(
                            entity.getInvoiceDate()));
        }

        if (entity.getRitesIcDate() != null) {
            dto.setRitesIcDate(
                    CommonUtils.convertDateToString(
                            entity.getRitesIcDate()));
        }

        if (entity.getRelaxationTestDate() != null) {
            dto.setRelaxationTestDate(
                    CommonUtils.convertDateToString(
                            entity.getRelaxationTestDate()));
        }

        if (entity.getRelaxationTestValidity() != null) {
            dto.setRelaxationTestValidity(
                    CommonUtils.convertDateToString(
                            entity.getRelaxationTestValidity()));
        }
        String status = sleeperWorkflowRepository
                .findLatestStatusByRequestIdAndModuleId(String.valueOf(entity.getId()), 5L)
                .orElse("NOT_STARTED");
        if (status != null) {
            dto.setStatus(status);
        }


        // ===== CHILD COILS =====

        if (entity.getCoilDetails() != null) {

            List<HtsCoilDetailsResponseDto> coilList =
                    entity.getCoilDetails()
                            .stream()
                            .map(c -> {

                                HtsCoilDetailsResponseDto cd =
                                        new HtsCoilDetailsResponseDto();

                                cd.setId(c.getId());
                                cd.setEntryType(c.getEntryType());
                                cd.setLotNo(c.getLotNo());
                                cd.setQtyKg(c.getQtyKg());

                                cd.setCoilNo(c.getCoilNo());
                                cd.setCoilFrom(c.getCoilFrom());
                                cd.setCoilTo(c.getCoilTo());

                                return cd;
                            })
                            .toList();

            dto.setCoilDetails(coilList);
        }


        return dto;
    }
}
