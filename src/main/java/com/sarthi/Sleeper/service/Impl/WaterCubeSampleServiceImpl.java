package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.WaterCubeSampleDetailDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.WaterCubeSampleRequestDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.WaterCubeSampleResponseDto;
import com.sarthi.Sleeper.entity.FinalInspection.WaterCubeSampleDeclaration;
import com.sarthi.Sleeper.entity.FinalInspection.WaterCubeSampleDetail;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.WaterCubeSampleRepository;
import com.sarthi.Sleeper.service.WaterCubeSampleService;
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
public class WaterCubeSampleServiceImpl implements WaterCubeSampleService {

    @Autowired
    private WaterCubeSampleRepository repository;

    // ================= CREATE =================

    @Override
    public WaterCubeSampleResponseDto create(WaterCubeSampleRequestDto dto) {

        WaterCubeSampleDeclaration entity = new WaterCubeSampleDeclaration();

        entity.setProductionDeclarationId(dto.getProductionDeclarationId());
        entity.setBatchNumber(dto.getBatchNumber());
        entity.setCastingDate(CommonUtils.convertStringToDateObject(dto.getCastingDate()));
        entity.setShift(dto.getShift());
        entity.setLineNo(dto.getLineNo());
        entity.setConcreteGrade(dto.getConcreteGrade());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());

        entity.setVendorCode(dto.getVendorCode());
        entity.setPlantId(dto.getPlantId());


        List<WaterCubeSampleDetail> detailList = new ArrayList<>();

        if (dto.getDetails() != null) {
            for (WaterCubeSampleDetailDto detailDto : dto.getDetails()) {
                WaterCubeSampleDetail detail = new WaterCubeSampleDetail();
                detail.setSampleNumber(detailDto.getSampleNumber());
                detail.setCubeNumber(detailDto.getCubeNumber());
                detail.setBenchNumber(detailDto.getBenchNumber());
                detail.setSequence(detailDto.getSequence());
                detail.setDeclaration(entity);
                detailList.add(detail);
            }
        }

        entity.setDetails(detailList);

        repository.save(entity);

        return buildResponse(entity);
    }

    // ================= UPDATE =================

    @Override
    public WaterCubeSampleResponseDto update(Long id, WaterCubeSampleRequestDto dto) {

        WaterCubeSampleDeclaration entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Water Cube Sample Declaration not found")));

        entity.setProductionDeclarationId(dto.getProductionDeclarationId());
        entity.setBatchNumber(dto.getBatchNumber());
        entity.setCastingDate(CommonUtils.convertStringToDateObject(dto.getCastingDate()));
        entity.setShift(dto.getShift());
        entity.setLineNo(dto.getLineNo());
        entity.setConcreteGrade(dto.getConcreteGrade());
        entity.setUpdatedBy(dto.getCreatedBy());
        entity.setUpdatedDate(LocalDateTime.now());


        entity.setVendorCode(dto.getVendorCode());
        entity.setPlantId(dto.getPlantId());

        entity.getDetails().clear();

        if (dto.getDetails() != null) {
            for (WaterCubeSampleDetailDto detailDto : dto.getDetails()) {
                WaterCubeSampleDetail detail = new WaterCubeSampleDetail();
                detail.setSampleNumber(detailDto.getSampleNumber());
                detail.setCubeNumber(detailDto.getCubeNumber());
                detail.setBenchNumber(detailDto.getBenchNumber());
                detail.setSequence(detailDto.getSequence());
                detail.setDeclaration(entity);
                entity.getDetails().add(detail);
            }
        }

        repository.save(entity);

        return buildResponse(entity);
    }

    // ================= GET BY ID =================

    @Override
    public WaterCubeSampleResponseDto getById(Long id) {

        WaterCubeSampleDeclaration entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Water Cube Sample Declaration not found")));

        return buildResponse(entity);
    }

    // ================= GET ALL =================

  /*  @Override
    public List<WaterCubeSampleResponseDto> getAll() {

        List<WaterCubeSampleResponseDto> list = new ArrayList<>();

        for (WaterCubeSampleDeclaration entity : repository.findAll()) {
            list.add(buildResponse(entity));
        }

        return list;
    } */
  @Override
  public List<WaterCubeSampleResponseDto> getAll() {

      List<WaterCubeSampleResponseDto> list = new ArrayList<>();

      for (WaterCubeSampleDeclaration entity : repository.findAllNotTested()) {
          list.add(buildResponse(entity));
      }

      return list;
  }

    // ================= GET BY USER =================

    @Override
    public List<WaterCubeSampleResponseDto> getByUser(Long userId) {

        List<WaterCubeSampleResponseDto> list = new ArrayList<>();

        for (WaterCubeSampleDeclaration entity : repository.findByCreatedBy(userId)) {
            list.add(buildResponse(entity));
        }

        return list;
    }

    // ================= DELETE =================

    @Override
    public void delete(Long id) {

        WaterCubeSampleDeclaration entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Water Cube Sample Declaration not found")));

        repository.deleteById(entity.getId());
    }

    // ================= RESPONSE BUILDER =================

    private WaterCubeSampleResponseDto buildResponse(WaterCubeSampleDeclaration entity) {

        WaterCubeSampleResponseDto dto = new WaterCubeSampleResponseDto();

        dto.setId(entity.getId());
        dto.setProductionDeclarationId(entity.getProductionDeclarationId());
        dto.setBatchNumber(entity.getBatchNumber());
        dto.setCastingDate(entity.getCastingDate() != null ? entity.getCastingDate().toString() : null);
        dto.setShift(entity.getShift());
        dto.setLineNo(entity.getLineNo());
        dto.setConcreteGrade(entity.getConcreteGrade());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());

        dto.setVendorCode(entity.getVendorCode());

        dto.setPlantId(entity.getPlantId());

        List<WaterCubeSampleDetailDto> detailDtos = new ArrayList<>();

        if (entity.getDetails() != null) {
            for (WaterCubeSampleDetail detail : entity.getDetails()) {
                WaterCubeSampleDetailDto detailDto = new WaterCubeSampleDetailDto();
                detailDto.setId(detail.getId());
                detailDto.setSampleNumber(detail.getSampleNumber());
                detailDto.setCubeNumber(detail.getCubeNumber());
                detailDto.setBenchNumber(detail.getBenchNumber());
                detailDto.setSequence(detail.getSequence());
                detailDtos.add(detailDto);
            }
        }

        dto.setDetails(detailDtos);

        return dto;
    }
}
