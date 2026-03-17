package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.Aggregates.AggregatesRequestDto;
import com.sarthi.Sleeper.dto.Aggregates.AggregatesResponseDto;
import com.sarthi.Sleeper.entity.AggregatesInventory;
import com.sarthi.Sleeper.repository.AggregatesInventoryRepository;
import com.sarthi.Sleeper.repository.SleeperWorkflowRepository;
import com.sarthi.Sleeper.service.AggregatesService;
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

public class AggregatesServiceImpl implements AggregatesService {

    @Autowired
    private AggregatesInventoryRepository repository;

    @Autowired
    private SleeperWorkflowRepository sleeperWorkflowRepository;

    // ================= CREATE =================

    @Override
    public AggregatesResponseDto create(AggregatesRequestDto dto) {

        AggregatesInventory entity = new AggregatesInventory();

        entity.setGradeSpec(dto.getGradeSpec());
        entity.setSource(dto.getSource());
        entity.setChallanNumber(dto.getChallanNumber());
        entity.setTotalQtyReceived(dto.getTotalQtyReceived());

        if (dto.getDateOfReceipt() != null) {
            entity.setDateOfReceipt(
                    CommonUtils.convertStringToDateObject(dto.getDateOfReceipt()));
        }

        if (dto.getChallanDate() != null) {
            entity.setChallanDate(
                    CommonUtils.convertStringToDateObject(dto.getChallanDate()));
        }

        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());

        AggregatesInventory saved = repository.save(entity);

        AggregatesResponseDto response = new AggregatesResponseDto();

        response.setId(saved.getId());
        response.setGradeSpec(saved.getGradeSpec());
        response.setSource(saved.getSource());
        response.setChallanNumber(saved.getChallanNumber());
        response.setTotalQtyReceived(saved.getTotalQtyReceived());
        response.setCreatedBy(saved.getCreatedBy());
        response.setCreatedDate(saved.getCreatedDate());

        if (saved.getDateOfReceipt() != null) {
            response.setDateOfReceipt(
                    CommonUtils.convertDateToString(saved.getDateOfReceipt()));
        }

        if (saved.getChallanDate() != null) {
            response.setChallanDate(
                    CommonUtils.convertDateToString(saved.getChallanDate()));
        }

        return response;
    }


    // ================= UPDATE =================

    @Override
    public AggregatesResponseDto update(Long id, AggregatesRequestDto dto) {

        AggregatesInventory entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Aggregates not found")
                ));


        entity.setGradeSpec(dto.getGradeSpec());
        entity.setSource(dto.getSource());
        entity.setChallanNumber(dto.getChallanNumber());
        entity.setTotalQtyReceived(dto.getTotalQtyReceived());

        if (dto.getDateOfReceipt() != null) {
            entity.setDateOfReceipt(
                    CommonUtils.convertStringToDateObject(dto.getDateOfReceipt()));
        }

        if (dto.getChallanDate() != null) {
            entity.setChallanDate(
                    CommonUtils.convertStringToDateObject(dto.getChallanDate()));
        }

        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        AggregatesInventory updated = repository.save(entity);

        AggregatesResponseDto response = new AggregatesResponseDto();

        response.setId(updated.getId());
        response.setGradeSpec(updated.getGradeSpec());
        response.setSource(updated.getSource());
        response.setChallanNumber(updated.getChallanNumber());
        response.setTotalQtyReceived(updated.getTotalQtyReceived());
        response.setUpdatedBy(updated.getUpdatedBy());
        response.setUpdatedDate(updated.getUpdatedDate());

        if (updated.getDateOfReceipt() != null) {
            response.setDateOfReceipt(
                    CommonUtils.convertDateToString(updated.getDateOfReceipt()));
        }

        if (updated.getChallanDate() != null) {
            response.setChallanDate(
                    CommonUtils.convertDateToString(updated.getChallanDate()));
        }

        return response;
    }


    // ================= GET BY ID =================

    @Override
    public AggregatesResponseDto getById(Long id) {

        AggregatesInventory entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Aggregates not found")
                ));

        AggregatesResponseDto response = new AggregatesResponseDto();

        response.setId(entity.getId());
        response.setGradeSpec(entity.getGradeSpec());
        response.setSource(entity.getSource());
        response.setChallanNumber(entity.getChallanNumber());
        response.setTotalQtyReceived(entity.getTotalQtyReceived());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setUpdatedDate(entity.getUpdatedDate());

        if (entity.getDateOfReceipt() != null) {
            response.setDateOfReceipt(
                    CommonUtils.convertDateToString(entity.getDateOfReceipt()));
        }

        if (entity.getChallanDate() != null) {
            response.setChallanDate(
                    CommonUtils.convertDateToString(entity.getChallanDate()));
        }

        return response;
    }


    // ================= GET ALL =================

    @Override
    public List<AggregatesResponseDto> getAll() {

        List<AggregatesResponseDto> list = new ArrayList<>();

        for (AggregatesInventory entity : repository.findAll()) {

            AggregatesResponseDto response = new AggregatesResponseDto();


            response.setId(entity.getId());
            response.setGradeSpec(entity.getGradeSpec());
            response.setSource(entity.getSource());
            response.setChallanNumber(entity.getChallanNumber());
            response.setTotalQtyReceived(entity.getTotalQtyReceived());
            response.setCreatedBy(entity.getCreatedBy());
            response.setCreatedDate(entity.getCreatedDate());
            response.setUpdatedBy(entity.getUpdatedBy());
            response.setUpdatedDate(entity.getUpdatedDate());

            String status = sleeperWorkflowRepository
                    .findLatestStatusByRequestIdAndModuleId(String.valueOf(entity.getId()), 8L)
                    .orElse("NOT_STARTED");
            if (status != null) {
                response.setStatus(status);
            }

            if (entity.getDateOfReceipt() != null) {
                response.setDateOfReceipt(
                        CommonUtils.convertDateToString(entity.getDateOfReceipt()));
            }

            if (entity.getChallanDate() != null) {
                response.setChallanDate(
                        CommonUtils.convertDateToString(entity.getChallanDate()));
            }

            list.add(response);
        }

        return list;
    }


    // ================= DELETE =================

    @Override
    public void delete(Long id) {
        AggregatesInventory entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Aggregates not found")
                ));

        repository.deleteById(entity.getId());
    }
}