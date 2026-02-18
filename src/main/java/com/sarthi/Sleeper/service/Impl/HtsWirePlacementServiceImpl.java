package com.sarthi.Sleeper.service.Impl;


import com.sarthi.Sleeper.dto.HtsWirePlacementRequestDTO;
import com.sarthi.Sleeper.dto.HtsWirePlacementResponseDTO;
import com.sarthi.Sleeper.entity.HtsWirePlacement;
import com.sarthi.Sleeper.repository.HtsWirePlacementRepository;
import com.sarthi.Sleeper.service.HtsWirePlacementService;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HtsWirePlacementServiceImpl implements HtsWirePlacementService {
    @Autowired
    private HtsWirePlacementRepository htsWirePlacementRepository;

    @Override
    public HtsWirePlacementResponseDTO create(
            HtsWirePlacementRequestDTO dto) {

        HtsWirePlacement entity = new HtsWirePlacement();

        entity.setLineShedNo(dto.getLineShedNo());

        LocalDate pDate = CommonUtils.convertStringToDateObject(dto.getPlacementDate());
        if (dto.getPlacementDate() != null) {
            entity.setPlacementDate(pDate);
        }

        entity.setPlacementTime(dto.getPlacementTime());

        entity.setBatchNo(dto.getBatchNo());
        entity.setBenchNo(dto.getBenchNo());
        entity.setSleeperType(dto.getSleeperType());
        entity.setNoOfWiresUsed(dto.getNoOfWiresUsed());
        entity.setHtsWireDiaMm(
                dto.getHtsWireDiaMm() != null ?
                        BigDecimal.valueOf(dto.getHtsWireDiaMm()) : null
        );
        entity.setLayLengthMm(
                dto.getLayLengthMm() != null ?
                        BigDecimal.valueOf(dto.getLayLengthMm()) : null
        );
        entity.setArrangementOk(dto.getArrangementOk());
        entity.setOverallStatus(dto.getOverallStatus());
        entity.setRemarks(dto.getRemarks());

        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());
        entity.setStatus("A");

        HtsWirePlacement saved =
                htsWirePlacementRepository.save(entity);

        return mapEntityToDto(saved);
    }

    @Override
    public HtsWirePlacementResponseDTO getById(Long id) {

        HtsWirePlacement entity =
                htsWirePlacementRepository.findById(id)
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Hts Wire Placement not found for the provided Id.")
                        ));
        return mapEntityToDto(entity);
    }


    @Override
    public List<HtsWirePlacementResponseDTO> getAll() {

        return htsWirePlacementRepository.findAll()
                .stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }


    @Override
    public HtsWirePlacementResponseDTO update(
            Long id,
            HtsWirePlacementRequestDTO dto) {

        HtsWirePlacement entity =
                htsWirePlacementRepository.findById(id)
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Hts Wire Placement not found for the provided Id.")
                        ));
        entity.setLineShedNo(dto.getLineShedNo());

        LocalDate pDate = CommonUtils.convertStringToDateObject(dto.getPlacementDate());
        if (dto.getPlacementDate() != null) {
            entity.setPlacementDate(pDate);
        }

        entity.setPlacementTime(dto.getPlacementTime());

        entity.setBatchNo(dto.getBatchNo());
        entity.setBenchNo(dto.getBenchNo());
        entity.setSleeperType(dto.getSleeperType());
        entity.setNoOfWiresUsed(dto.getNoOfWiresUsed());
        entity.setHtsWireDiaMm(
                dto.getHtsWireDiaMm() != null ?
                        BigDecimal.valueOf(dto.getHtsWireDiaMm()) : null
        );
        entity.setLayLengthMm(
                dto.getLayLengthMm() != null ?
                        BigDecimal.valueOf(dto.getLayLengthMm()) : null
        );
        entity.setArrangementOk(dto.getArrangementOk());
        entity.setOverallStatus(dto.getOverallStatus());
        entity.setRemarks(dto.getRemarks());

        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setStatus("A");

        entity.setUpdatedDate(LocalDateTime.now());

        HtsWirePlacement updated =
                htsWirePlacementRepository.save(entity);

        return mapEntityToDto(updated);
    }


    /* ============== DELETE ============== */

    @Override
    public void delete(Long id) {

        if (!htsWirePlacementRepository.existsById(id)) {
            throw new RuntimeException(
                    "Record not found : " + id);
        }

        htsWirePlacementRepository.deleteById(id);
    }




    private HtsWirePlacementResponseDTO mapEntityToDto(
            HtsWirePlacement entity) {

        HtsWirePlacementResponseDTO dto =
                new HtsWirePlacementResponseDTO();

        dto.setId(entity.getId());
        dto.setLineShedNo(entity.getLineShedNo());

       String pDate = CommonUtils.convertDateToString(entity.getPlacementDate());
        if (pDate != null) {
            dto.setPlacementDate(pDate);
        }

        dto.setPlacementTime(entity.getPlacementTime());

        dto.setBatchNo(entity.getBatchNo());
        dto.setBenchNo(entity.getBenchNo());
        dto.setSleeperType(entity.getSleeperType());
        dto.setNoOfWiresUsed(entity.getNoOfWiresUsed());

        if (entity.getHtsWireDiaMm() != null) {
            dto.setHtsWireDiaMm(
                    entity.getHtsWireDiaMm().doubleValue());
        }

        if (entity.getLayLengthMm() != null) {
            dto.setLayLengthMm(
                    entity.getLayLengthMm().doubleValue());
        }

        dto.setArrangementOk(entity.getArrangementOk());
        dto.setOverallStatus(entity.getOverallStatus());
        dto.setRemarks(entity.getRemarks());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedDate(entity.getUpdatedDate());
        dto.setStatus(entity.getStatus());

        return dto;
    }

}
