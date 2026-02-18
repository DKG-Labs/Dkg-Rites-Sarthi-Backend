package com.sarthi.Sleeper.service.Impl;


import com.sarthi.Sleeper.dto.MouldPreparationRequestDTO;
import com.sarthi.Sleeper.dto.MouldPreparationResponseDTO;
import com.sarthi.Sleeper.entity.MouldPreparation;
import com.sarthi.Sleeper.repository.MouldPreparationRepository;
import com.sarthi.Sleeper.service.MouldPreparationService;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MouldPreparationServiceImpl implements MouldPreparationService {
    @Autowired
    private MouldPreparationRepository mouldPreparationRepository;

    @Override
    public MouldPreparationResponseDTO create(
            MouldPreparationRequestDTO dto) {

        MouldPreparation entity = new MouldPreparation();


        entity.setLineShedNo(dto.getLineShedNo());

        LocalDate pDate = CommonUtils.convertStringToDateObject(dto.getPreparationDate());
        entity.setPreparationDate(pDate);

        LocalTime pTime = CommonUtils.convertStringToTimeObject(dto.getPreparationTime());
        entity.setPreparationTime(pTime);

        entity.setBatchNo(dto.getBatchNo());
        entity.setBenchNo(dto.getBenchNo());
        entity.setMouldCleaned(dto.getMouldCleaned());
        entity.setOilApplied(dto.getOilApplied());
        entity.setRemarks(dto.getRemarks());

        entity.setCreatedBy(dto.getCreatedBy());

        entity.setCreatedDate(LocalDateTime.now());
        entity.setStatus("A");

        MouldPreparation saved = mouldPreparationRepository.save(entity);

        return mapToResponse(saved);
    }




    @Override
    public MouldPreparationResponseDTO getById(Long id) {

        MouldPreparation entity =
                mouldPreparationRepository.findById(id)
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Mould Preparation not found for the provided Id.")
                        ));
        return mapToResponse(entity);
    }


    /* ================= UPDATE ================= */

    @Override
    public MouldPreparationResponseDTO update(
            Long id,
            MouldPreparationRequestDTO dto) {

        MouldPreparation entity =
               mouldPreparationRepository.findById(id)
                       .orElseThrow(() -> new BusinessException(
                               new ErrorDetails(
                                       AppConstant.ERROR_CODE_RESOURCE,
                                       AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                       AppConstant.ERROR_TYPE_VALIDATION,
                                       "Mould Preparation not found for the provided Id.")
                       ));


        entity.setLineShedNo(dto.getLineShedNo());

        LocalDate pDate = CommonUtils.convertStringToDateObject(dto.getPreparationDate());
        entity.setPreparationDate(pDate);

        LocalTime pTime = CommonUtils.convertStringToTimeObject(dto.getPreparationTime());
        entity.setPreparationTime(pTime);

        entity.setBatchNo(dto.getBatchNo());
        entity.setBenchNo(dto.getBenchNo());
        entity.setMouldCleaned(dto.getMouldCleaned());
        entity.setOilApplied(dto.getOilApplied());
        entity.setRemarks(dto.getRemarks());

        entity.setUpdatedBy(dto.getUpdateBy());
        entity.setUpdatedDate(LocalDateTime.now());

        MouldPreparation updated = mouldPreparationRepository.save(entity);

        return mapToResponse(updated);
    }


    /* ================= ENTITY → RESPONSE DTO ================= */

    private MouldPreparationResponseDTO mapToResponse(
            MouldPreparation entity) {

        MouldPreparationResponseDTO dto =
                new MouldPreparationResponseDTO();

        dto.setId(entity.getId());
        dto.setLineShedNo(entity.getLineShedNo());

        String pDate = CommonUtils.convertDateToString(entity.getPreparationDate());
        dto.setPreparationDate(pDate);

        LocalTime pTime = entity.getPreparationTime();
        dto.setPreparationTime(pTime);


        dto.setBatchNo(entity.getBatchNo());
        dto.setBenchNo(entity.getBenchNo());
        dto.setMouldCleaned(entity.getMouldCleaned());
        dto.setOilApplied(entity.getOilApplied());
        dto.setRemarks(entity.getRemarks());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedDate(entity.getUpdatedDate());

        dto.setStatus(entity.getStatus());

        return dto;
    }

    @Override
    public List<MouldPreparationResponseDTO> getAll() {

        List<MouldPreparation> list =
                mouldPreparationRepository.findAll();

        return list.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {

        MouldPreparation entity =
               mouldPreparationRepository.findById(id)
                       .orElseThrow(() -> new BusinessException(
                               new ErrorDetails(
                                       AppConstant.ERROR_CODE_RESOURCE,
                                       AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                       AppConstant.ERROR_TYPE_VALIDATION,
                                       "Mould Preparation not found for the provided Id.")
                       ));
        mouldPreparationRepository.delete(entity);
    }


}
