package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.RawMaterialSourceRequestDto;
import com.sarthi.Sleeper.dto.RawMaterialSourceResponseDto;
import com.sarthi.Sleeper.entity.RawMaterialSource;
import com.sarthi.Sleeper.repository.RawMaterialSourceRepository;
import com.sarthi.Sleeper.repository.SleeperWorkflowRepository;
import com.sarthi.Sleeper.service.RawMaterialSourceService;
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
public class RawMaterialSourceServiceImpl implements RawMaterialSourceService {

    @Autowired
    private RawMaterialSourceRepository repository;
    @Autowired
    private SleeperWorkflowRepository sleeperWorkflowRepository;


    // CREATE
        @Override
        public RawMaterialSourceResponseDto create(RawMaterialSourceRequestDto dto) {

            RawMaterialSource entity = new RawMaterialSource();

            entity.setRawMaterialType(dto.getRawMaterialType());
            entity.setSupplierName(dto.getSupplierName());
            entity.setApprovalReference(dto.getApprovalReference());

            if (dto.getValidFrom() != null) {
                entity.setValidFrom(
                        CommonUtils.convertStringToDateObject(dto.getValidFrom()));
            }

            if (dto.getValidTo() != null) {
                entity.setValidTo(
                        CommonUtils.convertStringToDateObject(dto.getValidTo()));
            }

            entity.setCreatedBy(dto.getCreatedBy());
            entity.setCreatedDate(LocalDateTime.now());

            repository.save(entity);

            return buildResponse(entity);
        }


        // UPDATE
        @Override
        public RawMaterialSourceResponseDto update(Long id, RawMaterialSourceRequestDto dto) {

            RawMaterialSource entity = repository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Source not found")));

            entity.setRawMaterialType(dto.getRawMaterialType());
            entity.setSupplierName(dto.getSupplierName());
            entity.setApprovalReference(dto.getApprovalReference());

            if (dto.getValidFrom() != null) {
                entity.setValidFrom(
                        CommonUtils.convertStringToDateObject(dto.getValidFrom()));
            }

            if (dto.getValidTo() != null) {
                entity.setValidTo(
                        CommonUtils.convertStringToDateObject(dto.getValidTo()));
            }

            entity.setUpdatedBy(dto.getUpdatedBy());
            entity.setUpdatedDate(LocalDateTime.now());

            repository.save(entity);

            return buildResponse(entity);
        }


        // GET BY ID
        @Override
        public RawMaterialSourceResponseDto getById(Long id) {

            RawMaterialSource entity = repository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Source not found")));

            return buildResponse(entity);
        }


        // GET ALL
        @Override
        public List<RawMaterialSourceResponseDto> getAll() {

            List<RawMaterialSourceResponseDto> list = new ArrayList<>();

            for (RawMaterialSource entity : repository.findAll()) {
                list.add(buildResponse(entity));
            }

            return list;
        }


        // DELETE
        @Override
        public void delete(Long id) {

            RawMaterialSource entity = repository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Source not found")));
            repository.deleteById(entity.getId());
        }


        // RESPONSE BUILDER
        private RawMaterialSourceResponseDto buildResponse(RawMaterialSource entity) {

            RawMaterialSourceResponseDto dto = new RawMaterialSourceResponseDto();

            dto.setId(entity.getId());
            dto.setRawMaterialType(entity.getRawMaterialType());
            dto.setSupplierName(entity.getSupplierName());
            dto.setApprovalReference(entity.getApprovalReference());

            if (entity.getValidFrom() != null) {
                dto.setValidFrom(CommonUtils.convertDateToString(entity.getValidFrom()));
            }

            if (entity.getValidTo() != null) {
                dto.setValidTo(CommonUtils.convertDateToString(entity.getValidTo()));
            }

            dto.setCreatedBy(entity.getCreatedBy());
            dto.setCreatedDate(entity.getCreatedDate());
            dto.setUpdatedBy(entity.getUpdatedBy());
            dto.setUpdatedDate(entity.getUpdatedDate());
            String status = sleeperWorkflowRepository
                    .findLatestStatusByRequestIdAndModuleId(String.valueOf(entity.getId()), 3L)
                    .orElse("NOT_STARTED");
            if (status != null) {
                dto.setStatus(status);
            }

            return dto;
        }



}
