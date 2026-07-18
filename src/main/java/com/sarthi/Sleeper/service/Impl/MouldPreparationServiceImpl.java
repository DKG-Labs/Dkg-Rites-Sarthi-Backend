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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
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

        entity.setVendorCode(dto.getVendorCode());
        entity.setPlantId(dto.getPlantId());
        entity.setShift(dto.getShift());
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
        entity.setVendorCode(dto.getVendorCode());
        entity.setPlantId(dto.getPlantId());
        entity.setShift(dto.getShift());
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
        dto.setVendorCode(entity.getVendorCode());
        dto.setPlantId(entity.getPlantId());
        dto.setShift(entity.getShift());

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
/*
    @Override
    public List<MouldPreparationResponseDTO> getRecordsByDate(
          String plantId,
            String vendorCode,
            String shift,
            int createdBy,
            String date) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate selectedDate = LocalDate.parse(date, formatter);

        LocalDateTime startOfDay = selectedDate.atStartOfDay();
        LocalDateTime endOfDay = selectedDate.atTime(23, 59, 59);

        List<MouldPreparation> list = mouldPreparationRepository.findByDate(
                plantId,
                vendorCode,
                shift,
                createdBy,
                startOfDay,
                endOfDay
        );

        return list.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
*/
@Override
public List<MouldPreparationResponseDTO> getRecordsByDate(
        String plantId,
        String vendorCode,
        String shift,
        int createdBy,
        String date) {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    LocalDate selectedDate = LocalDate.parse(date, formatter);

    LocalDateTime startOfDay;
    LocalDateTime endOfDay;

    if ("C".equalsIgnoreCase(shift)) {
        // Shift C IST: 10 PM → 6 AM next day
        // DB stores LocalDateTime.now() in UTC (serverTimezone=UTC)
        // IST 22:00 = UTC 16:30 | IST 06:00 next day = UTC 00:30 next day
        startOfDay = selectedDate.atTime(16, 30, 0);
        endOfDay = selectedDate.plusDays(1).atTime(0, 30, 0);
    } else {
        // Shift A & B → normal day
        startOfDay = selectedDate.atStartOfDay();
        endOfDay = selectedDate.atTime(23, 59, 59);
    }

    List<MouldPreparation> list = mouldPreparationRepository.findByDate(
            plantId,
            vendorCode,
            shift,
            createdBy,
            startOfDay,
            endOfDay
    );

    return list.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
}

}
