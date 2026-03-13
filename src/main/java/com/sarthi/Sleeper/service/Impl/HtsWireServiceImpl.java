package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.HtsWire.HtsCoilDetailsRequestDto;
import com.sarthi.Sleeper.dto.HtsWire.HtsCoilDetailsResponseDto;
import com.sarthi.Sleeper.dto.HtsWire.HtsWireRequestDto;
import com.sarthi.Sleeper.dto.HtsWire.HtsWireResponseDto;
import com.sarthi.Sleeper.entity.DowelInventory;
import com.sarthi.Sleeper.entity.VendorHtsWire.HtsCoilDetails;
import com.sarthi.Sleeper.entity.VendorHtsWire.HtsWire;
import com.sarthi.Sleeper.repository.HtsWireRepository;
import com.sarthi.Sleeper.service.HtsWireService;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HtsWireServiceImpl implements HtsWireService {

    private final HtsWireRepository repository;

    // ================= CREATE =================

    @Override
    public HtsWireResponseDto create(HtsWireRequestDto dto) {

        HtsWire entity = new HtsWire();

        mapBasicFields(entity, dto);

        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());

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

        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
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
    }


    // ================= BASIC FIELD MAPPER =================

    private void mapBasicFields(HtsWire entity,
                                HtsWireRequestDto dto) {

        entity.setGradeSpec(dto.getGradeSpec());
        entity.setManufacturer(dto.getManufacturer());

        entity.setInvoiceNumber(dto.getInvoiceNumber());
        entity.setRitesIcNumber(dto.getRitesIcNumber());

        entity.setRelaxationTest(dto.getRelaxationTest());

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


    // ================= RESPONSE MAPPER =================

    private HtsWireResponseDto mapToResponse(HtsWire entity) {

        HtsWireResponseDto dto =
                new HtsWireResponseDto();

        dto.setId(entity.getId());
        dto.setGradeSpec(entity.getGradeSpec());
        dto.setManufacturer(entity.getManufacturer());

        dto.setInvoiceNumber(entity.getInvoiceNumber());
        dto.setRitesIcNumber(entity.getRitesIcNumber());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setRelaxationTest(entity.getRelaxationTest());
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
