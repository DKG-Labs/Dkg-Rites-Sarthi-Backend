package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.AdmixtureRequestDto;
import com.sarthi.Sleeper.dto.AdmixtureResponseDto;

import com.sarthi.Sleeper.entity.AdmixtureInventory;
import com.sarthi.Sleeper.repository.AdmixtureInventoryRepository;
import com.sarthi.Sleeper.repository.SleeperWorkflowRepository;
import com.sarthi.Sleeper.service.AdmixtureInventoryService;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdmixtureServiceImpl implements AdmixtureInventoryService {

    @Autowired
    private AdmixtureInventoryRepository admixtureInventoryRepository;
    @Autowired
    private SleeperWorkflowRepository sleeperWorkflowRepository;

    // ================= CREATE =================

    @Override
    public AdmixtureResponseDto create(AdmixtureRequestDto dto) {

        AdmixtureInventory entity = new AdmixtureInventory();

        entity.setManufacturer(dto.getManufacturer());
        entity.setGradeSpec(dto.getGradeSpec());
        entity.setInvoiceNumber(dto.getInvoiceNumber());
        entity.setLotNo(dto.getLotNo());
        entity.setMtcNo(dto.getMtcNo());
        entity.setTotalQuantity(dto.getTotalQuantity());

        if (dto.getDateOfReceipt() != null) {
            entity.setDateOfReceipt(
                    CommonUtils.convertStringToDateObject(dto.getDateOfReceipt()));
        }

        if (dto.getInvoiceDate() != null) {
            entity.setInvoiceDate(
                    CommonUtils.convertStringToDateObject(dto.getInvoiceDate()));
        }

        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());

        AdmixtureInventory saved = admixtureInventoryRepository.save(entity);

        AdmixtureResponseDto response = new AdmixtureResponseDto();

        response.setId(saved.getId());
        response.setManufacturer(saved.getManufacturer());
        response.setGradeSpec(saved.getGradeSpec());
        response.setInvoiceNumber(saved.getInvoiceNumber());
        response.setLotNo(saved.getLotNo());
        response.setMtcNo(saved.getMtcNo());
        response.setTotalQuantity(saved.getTotalQuantity());
        response.setCreatedBy(saved.getCreatedBy());
        response.setCreatedDate(saved.getCreatedDate());

        if (saved.getDateOfReceipt() != null) {
            response.setDateOfReceipt(
                    CommonUtils.convertDateToString(saved.getDateOfReceipt()));
        }

        if (saved.getInvoiceDate() != null) {
            response.setInvoiceDate(
                    CommonUtils.convertDateToString(saved.getInvoiceDate()));
        }

        return response;
    }


    // ================= UPDATE =================

    @Override
    public AdmixtureResponseDto update(Long id, AdmixtureRequestDto dto) {

        AdmixtureInventory entity = admixtureInventoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Admixture not found")
                ));


        entity.setManufacturer(dto.getManufacturer());
        entity.setGradeSpec(dto.getGradeSpec());
        entity.setInvoiceNumber(dto.getInvoiceNumber());
        entity.setLotNo(dto.getLotNo());
        entity.setMtcNo(dto.getMtcNo());
        entity.setTotalQuantity(dto.getTotalQuantity());

        if (dto.getDateOfReceipt() != null) {
            entity.setDateOfReceipt(
                    CommonUtils.convertStringToDateObject(dto.getDateOfReceipt()));
        }

        if (dto.getInvoiceDate() != null) {
            entity.setInvoiceDate(
                    CommonUtils.convertStringToDateObject(dto.getInvoiceDate()));
        }

        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        AdmixtureInventory updated = admixtureInventoryRepository.save(entity);

        AdmixtureResponseDto response = new AdmixtureResponseDto();

        response.setId(updated.getId());
        response.setManufacturer(updated.getManufacturer());
        response.setGradeSpec(updated.getGradeSpec());
        response.setInvoiceNumber(updated.getInvoiceNumber());
        response.setLotNo(updated.getLotNo());
        response.setMtcNo(updated.getMtcNo());
        response.setTotalQuantity(updated.getTotalQuantity());
        response.setUpdatedBy(updated.getUpdatedBy());
        response.setUpdatedDate(updated.getUpdatedDate());

        if (updated.getDateOfReceipt() != null) {
            response.setDateOfReceipt(
                    CommonUtils.convertDateToString(updated.getDateOfReceipt()));
        }

        if (updated.getInvoiceDate() != null) {
            response.setInvoiceDate(
                    CommonUtils.convertDateToString(updated.getInvoiceDate()));
        }

        return response;
    }


    // ================= GET BY ID =================

    @Override
    public AdmixtureResponseDto getById(Long id) {

        AdmixtureInventory entity = admixtureInventoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Admixture not found")
                ));
        AdmixtureResponseDto response = new AdmixtureResponseDto();

        response.setId(entity.getId());
        response.setManufacturer(entity.getManufacturer());
        response.setGradeSpec(entity.getGradeSpec());
        response.setInvoiceNumber(entity.getInvoiceNumber());
        response.setLotNo(entity.getLotNo());
        response.setMtcNo(entity.getMtcNo());
        response.setTotalQuantity(entity.getTotalQuantity());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setUpdatedDate(entity.getUpdatedDate());

        if (entity.getDateOfReceipt() != null) {
            response.setDateOfReceipt(
                    CommonUtils.convertDateToString(entity.getDateOfReceipt()));
        }

        if (entity.getInvoiceDate() != null) {
            response.setInvoiceDate(
                    CommonUtils.convertDateToString(entity.getInvoiceDate()));
        }

        return response;
    }


    // ================= GET ALL =================

    @Override
    public List<AdmixtureResponseDto> getAll() {

        List<AdmixtureResponseDto> list = new ArrayList<>();

        for (AdmixtureInventory entity : admixtureInventoryRepository.findAll()) {

            AdmixtureResponseDto response = new AdmixtureResponseDto();

            response.setId(entity.getId());
            response.setManufacturer(entity.getManufacturer());
            response.setGradeSpec(entity.getGradeSpec());
            response.setInvoiceNumber(entity.getInvoiceNumber());
            response.setLotNo(entity.getLotNo());
            response.setMtcNo(entity.getMtcNo());
            response.setTotalQuantity(entity.getTotalQuantity());
            response.setCreatedBy(entity.getCreatedBy());
            response.setCreatedDate(entity.getCreatedDate());
            response.setUpdatedBy(entity.getUpdatedBy());
            response.setUpdatedDate(entity.getUpdatedDate());

            if (entity.getDateOfReceipt() != null) {
                response.setDateOfReceipt(
                        CommonUtils.convertDateToString(entity.getDateOfReceipt()));
            }

            if (entity.getInvoiceDate() != null) {
                response.setInvoiceDate(
                        CommonUtils.convertDateToString(entity.getInvoiceDate()));
            }

            String status = sleeperWorkflowRepository
                    .findLatestStatusByRequestIdAndModuleId(String.valueOf(entity.getId()), 7L)
                    .orElse("NOT_STARTED");
            if (status != null) {
                response.setStatus(status);
            }

            list.add(response);
        }

        return list;
    }


    // ================= DELETE =================

    @Override
    public void delete(Long id) {
        AdmixtureInventory entity = admixtureInventoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Admixture not found")
                ));

        admixtureInventoryRepository.deleteById(entity.getId());
    }
}
