package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.MomentOfResistanceRequestDTO;
import com.sarthi.Sleeper.dto.MomentOfResistanceResponseDTO;
import com.sarthi.Sleeper.entity.MomentOfResistance;
import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionDeclaration;
import com.sarthi.Sleeper.repository.MomentOfResistanceRepository;
import com.sarthi.Sleeper.repository.ProductionDeclaration.ProductionDeclarationRepository;
import com.sarthi.Sleeper.service.MomentOfResistanceService;
import com.sarthi.Sleeper.service.WaterCubeLotService;
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

        @Autowired
        private ProductionDeclarationRepository productionDeclarationRepository;

        /** Lot rejection (shared with the water cube test): a rejected lot gets no new MR samples */
        @Autowired
        private WaterCubeLotService lotService;

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

            // Round (Fresh / Retest = double testing) and lot of the samples
            entity.setMrTestType(dto.getMrTestType());
            if (dto.getProductionDeclarationId() != null) {
                entity.setProductionDeclarationId(requireActiveLotOfBatch(dto.getProductionDeclarationId(), dto.getBatchNumber()));
            }

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

            // mrTestType and productionDeclarationId are fixed when the samples are declared and are not changed here:
            // the MR page marks a tested first-test record as "Retest" through this update, which must not turn it
            // into a double-testing declaration.

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

            dto.setCreatedBy(entity.getCreatedBy());
            dto.setCreatedDate(entity.getCreatedDate());

            dto.setUpdatedBy(entity.getUpdatedBy());
            dto.setUpdatedDate(entity.getUpdatedDate());

            dto.setMrTestType(entity.getMrTestType());
            dto.setProductionDeclarationId(entity.getProductionDeclarationId());

            return dto;
        }

        /**
         * The production batch (lot) with this id, checked to exist, to carry the same batch number as the MR samples
         * and not to be rejected. Returns its id.
         */
        private Long requireActiveLotOfBatch(Long productionDeclarationId, String batchNumber) {
            ProductionDeclaration pd = productionDeclarationRepository.findById(productionDeclarationId)
                    .orElseThrow(() -> validation("Production batch " + productionDeclarationId + " not found for the MR samples."));
            String lotBatch = pd.getBatchNumber() != null ? pd.getBatchNumber().trim() : "";
            String sampleBatch = batchNumber != null ? batchNumber.trim() : "";
            if (!lotBatch.equalsIgnoreCase(sampleBatch)) {
                throw validation("Production batch " + productionDeclarationId + " is batch " + lotBatch
                        + ", not " + sampleBatch + ".");
            }
            lotService.assertNotRejected(pd.getId());
            return pd.getId();
        }

        private static BusinessException validation(String message) {
            return new BusinessException(new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_VALIDATION,
                    message));
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
            // Shift C IST: 10 PM → 9 AM next day
            // DB stores LocalDateTime.now() in UTC (serverTimezone=UTC)
            // IST 22:00 = UTC 16:30 | IST 09:00 next day = UTC 03:30 next day
            startOfDay = selectedDate.atTime(16, 30, 0);
            endOfDay = selectedDate.plusDays(1).atTime(3, 30, 0);
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
