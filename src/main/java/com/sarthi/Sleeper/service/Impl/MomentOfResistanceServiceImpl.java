package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.MomentOfResistanceRequestDTO;
import com.sarthi.Sleeper.dto.MomentOfResistanceResponseDTO;
import com.sarthi.Sleeper.entity.MomentOfResistance;
import com.sarthi.Sleeper.repository.MomentOfResistanceRepository;
import com.sarthi.Sleeper.service.MomentOfResistanceService;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.expression.DenyAllPermissionEvaluator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MomentOfResistanceServiceImpl implements MomentOfResistanceService {


        @Autowired
        private MomentOfResistanceRepository repository;

        /* ================= CREATE ================= */

        @Override
        public MomentOfResistanceResponseDTO create(
                MomentOfResistanceRequestDTO dto) {

            MomentOfResistance entity = new MomentOfResistance();

            entity.setBatchNumber(dto.getBatchNumber());
            entity.setSleeperType(dto.getSleeperType());
            entity.setBenchNumber(dto.getBenchNumber());
            entity.setSleeperNo(dto.getSleeperNo());
            entity.setTestResult(dto.getTestResult());
            entity.setRemarks(dto.getRemarks());

            entity.setStatus("Created");
            entity.setVendorCode(dto.getVendorCode());
            entity.setPlantId(dto.getPlantId());
            entity.setShift(dto.getShift());
            entity.setCreatedBy(dto.getCreatedBy());

            entity.setCreatedDate(LocalDateTime.now());

            MomentOfResistance saved = repository.save(entity);

            return mapToResponse(saved);
        }

        /* ================= GET BY ID ================= */

        @Override
        public MomentOfResistanceResponseDTO getById(Long id) {

            MomentOfResistance entity = repository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "MR record not found for the provided Id.")
                    ));

            return mapToResponse(entity);
        }

        /* ================= UPDATE ================= */

        @Override
        public MomentOfResistanceResponseDTO update(
                Long id,
                MomentOfResistanceRequestDTO dto) {

            MomentOfResistance entity = repository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "MR record not found for the provided Id.")
                    ));

            entity.setBatchNumber(dto.getBatchNumber());
            entity.setSleeperType(dto.getSleeperType());
            entity.setBenchNumber(dto.getBenchNumber());
            entity.setSleeperNo(dto.getSleeperNo());
            entity.setTestResult(dto.getTestResult());
            entity.setRemarks(dto.getRemarks());

            entity.setStatus("Updated");
            entity.setVendorCode(dto.getVendorCode());
            entity.setPlantId(dto.getPlantId());
            entity.setShift(dto.getShift());

            entity.setUpdatedBy(dto.getUpdatedBy());
            entity.setUpdatedDate(LocalDateTime.now());

            MomentOfResistance updated = repository.save(entity);

            return mapToResponse(updated);
        }

        /* ================= GET ALL ================= */

        @Override
        public List<MomentOfResistanceResponseDTO> getAll() {

            List<MomentOfResistance> list = repository.findAll();

            return list.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        /* ================= DELETE ================= */

        @Override
        public void delete(Long id) {

            MomentOfResistance entity = repository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "MR record not found for the provided Id.")
                    ));

            repository.delete(entity);
        }

        /* ================= MAPPER ================= */

        private MomentOfResistanceResponseDTO mapToResponse(
                MomentOfResistance entity) {

            MomentOfResistanceResponseDTO dto =
                    new MomentOfResistanceResponseDTO();

            dto.setId(entity.getId());
            dto.setBatchNumber(entity.getBatchNumber());
            dto.setSleeperType(entity.getSleeperType());
            dto.setBenchNumber(entity.getBenchNumber());
            dto.setSleeperNo(entity.getSleeperNo());
            dto.setTestResult(entity.getTestResult());
            dto.setRemarks(entity.getRemarks());

            dto.setVendorCode(entity.getVendorCode());
            dto.setPlantId(entity.getPlantId());
            dto.setShift(entity.getShift());
            dto.setShift(entity.getStatus());

            dto.setCreatedBy(entity.getCreatedBy());
            dto.setCreatedDate(entity.getCreatedDate());

            dto.setUpdatedBy(entity.getUpdatedBy());
            dto.setUpdatedDate(entity.getUpdatedDate());

            return dto;
        }


    @Override
    public List<MomentOfResistanceResponseDTO> getRecordsByDate(
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
            // Shift C → 10 PM to next day 6 AM
            startOfDay = selectedDate.atTime(22, 0, 0);
            endOfDay = selectedDate.plusDays(1).atTime(6, 0, 0);
        } else {
            // Shift A & B
            startOfDay = selectedDate.atStartOfDay();
            endOfDay = selectedDate.atTime(23, 59, 59);
        }

        List<MomentOfResistance> list =
                repository.findByDateExcludingTested(
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
