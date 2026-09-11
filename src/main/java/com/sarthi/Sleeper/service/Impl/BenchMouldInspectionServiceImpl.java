package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.BenchMouldDtos.BenchMouldInspectionRequestDto;
import com.sarthi.Sleeper.dto.BenchMouldDtos.BenchMouldInspectionResponseDto;
import com.sarthi.Sleeper.entity.BatchWeighment.BatchWeighment;
import com.sarthi.Sleeper.entity.BenchMouldInspection;
import com.sarthi.Sleeper.repository.BenchMouldInspectionRepository;
import com.sarthi.Sleeper.service.BenchMouldInspectionService;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.util.CommonUtils;
import com.sarthi.repository.UserMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class BenchMouldInspectionServiceImpl
        implements BenchMouldInspectionService {
    @Autowired
    private BenchMouldInspectionRepository repository;
    
    @Autowired
    private UserMasterRepository userMasterRepository;


    @Override
    public BenchMouldInspectionResponseDto create(
            BenchMouldInspectionRequestDto dto) {

        BenchMouldInspection entity =
                new BenchMouldInspection();

        entity.setLineShedNo(dto.getLineShedNo());

        LocalDate checkDate =
                CommonUtils.convertStringToDateObject(
                        dto.getCheckingDate());

        entity.setCheckingDate(checkDate);

        entity.setBenchGangNo(dto.getBenchGangNo());
        entity.setSleeperType(dto.getSleeperType());

        LocalDate castingDate =
                CommonUtils.convertStringToDateObject(
                        dto.getLatestCastingDate());

        entity.setLatestCastingDate(castingDate);

        entity.setBenchVisualResult(
                dto.getBenchVisualResult());
        entity.setBenchDimensionalResult(
                dto.getBenchDimensionalResult());

        entity.setMouldVisualResult(
                dto.getMouldVisualResult());
        entity.setMouldDimensionalResult(
                dto.getMouldDimensionalResult());

        entity.setCombinedRemarks(
                dto.getCombinedRemarks());

        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());
        entity.setStatus("A");

        BenchMouldInspection saved =
                repository.save(entity);

        return mapToResponse(saved);
    }


    @Override
    public BenchMouldInspectionResponseDto update(
            Long id,
            BenchMouldInspectionRequestDto dto) {

        BenchMouldInspection entity =
                repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Inspection not found.")
                ));

        entity.setLineShedNo(dto.getLineShedNo());

        if (dto.getCheckingDate() != null) {
            entity.setCheckingDate(
                    CommonUtils.convertStringToDateObject(
                            dto.getCheckingDate()));
        }

        entity.setBenchGangNo(dto.getBenchGangNo());
        entity.setSleeperType(dto.getSleeperType());

        if (dto.getLatestCastingDate() != null) {
            entity.setLatestCastingDate(
                    CommonUtils.convertStringToDateObject(
                            dto.getLatestCastingDate()));
        }

        entity.setBenchVisualResult(
                dto.getBenchVisualResult());
        entity.setBenchDimensionalResult(
                dto.getBenchDimensionalResult());

        entity.setMouldVisualResult(
                dto.getMouldVisualResult());
        entity.setMouldDimensionalResult(
                dto.getMouldDimensionalResult());

        entity.setCombinedRemarks(
                dto.getCombinedRemarks());

        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        BenchMouldInspection updated =
                repository.save(entity);

        return mapToResponse(updated);
    }


    @Override
    public BenchMouldInspectionResponseDto getById(
            Long id) {

        BenchMouldInspection entity =
                repository.findById(id)
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Inspection not found.")
                        ));
        return mapToResponse(entity);
    }


    @Override
    public List<BenchMouldInspectionResponseDto> getAll() {
        List<BenchMouldInspection> list = repository.findAll();
        if (list.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        java.util.List<Integer> userIds = list.stream()
                .map(BenchMouldInspection::getCreatedBy)
                .filter(java.util.Objects::nonNull)
                .map(s -> {
                    try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
                })
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        java.util.Map<Integer, String> userMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            userMasterRepository.findByUserIdIn(userIds).forEach(u -> {
                userMap.put(u.getUserId(), u.getUsername());
            });
        }

        return list.stream()
                .map(entity -> mapToResponseWithUserMap(entity, userMap))
                .toList();
    }

    private BenchMouldInspectionResponseDto mapToResponseWithUserMap(
            BenchMouldInspection entity,
            java.util.Map<Integer, String> userMap) {
        BenchMouldInspectionResponseDto dto = mapToResponseBasic(entity);
        if (entity.getCreatedBy() != null) {
            try {
                int createdById = Integer.parseInt(entity.getCreatedBy().trim());
                dto.setCreatedBy(createdById);
                if (userMap != null && userMap.containsKey(createdById)) {
                    dto.setUserName(userMap.get(createdById));
                }
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        return dto;
    }

    @Override
    public void delete(Long id) {
        BenchMouldInspection entity =
                repository.findById(id)
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Inspection not found.")
                        ));
        repository.deleteById(entity.getId());
    }

    private BenchMouldInspectionResponseDto mapToResponse(
            BenchMouldInspection entity) {
        BenchMouldInspectionResponseDto dto = mapToResponseBasic(entity);
        if (entity.getCreatedBy() != null) {
            try {
                int createdById = Integer.parseInt(entity.getCreatedBy().trim());
                dto.setCreatedBy(createdById);
                userMasterRepository.findByUserId(createdById).ifPresent(user -> {
                    dto.setUserName(user.getUsername());
                });
            } catch (NumberFormatException e) {
                // Log or handle if createdBy is not an integer
            }
        }
        return dto;
    }

    private BenchMouldInspectionResponseDto mapToResponseBasic(
            BenchMouldInspection entity) {

        BenchMouldInspectionResponseDto dto =
                new BenchMouldInspectionResponseDto();

        dto.setId(entity.getId());
        dto.setLineShedNo(entity.getLineShedNo());

        if (entity.getCheckingDate() != null) {
            dto.setCheckingDate(
                    CommonUtils.convertDateToString(
                            entity.getCheckingDate()));
        }

        dto.setBenchGangNo(entity.getBenchGangNo());
        dto.setSleeperType(entity.getSleeperType());

        if (entity.getLatestCastingDate() != null) {
            dto.setLatestCastingDate(
                    CommonUtils.convertDateToString(
                            entity.getLatestCastingDate()));
        }

        dto.setBenchVisualResult(
                entity.getBenchVisualResult());
        dto.setBenchDimensionalResult(
                entity.getBenchDimensionalResult());

        dto.setMouldVisualResult(
                entity.getMouldVisualResult());
        dto.setMouldDimensionalResult(
                entity.getMouldDimensionalResult());

        dto.setCombinedRemarks(
                entity.getCombinedRemarks());

        return dto;
    }
}
