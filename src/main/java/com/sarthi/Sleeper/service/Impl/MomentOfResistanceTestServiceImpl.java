package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.MomentOfResistanceDetailResponseDTO;
import com.sarthi.Sleeper.dto.MomentOfResistanceTestRequestDTO;
import com.sarthi.Sleeper.dto.MomentOfResistanceTestResponseDTO;
import com.sarthi.Sleeper.entity.MomentOfResistance;
import com.sarthi.Sleeper.entity.MomentOfResistanceDetail;
import com.sarthi.Sleeper.entity.MomentOfResistanceTest;
import com.sarthi.Sleeper.entity.ProductionDeclaration.LotRejectionTestType;
import com.sarthi.Sleeper.repository.MomentOfResistanceRepository;
import com.sarthi.Sleeper.repository.MomentOfResistanceTestRepository;
import com.sarthi.Sleeper.service.MomentOfResistanceTestService;
import com.sarthi.Sleeper.service.WaterCubeLotService;
import com.sarthi.config.MrLotRejectionProperties;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MomentOfResistanceTestServiceImpl implements MomentOfResistanceTestService {

    @Autowired
    private MomentOfResistanceTestRepository repository;
@Autowired
    private MomentOfResistanceRepository momentOfResistanceRepository;

    /** Lot rejection (shared with the water cube test) */
    @Autowired
    private WaterCubeLotService lotService;

    @Autowired
    private MrLotRejectionProperties mrRules;

    /* ================= CREATE ================= */

    // The result, the declaration status and a lot rejection commit or roll back together
    @Override
    @Transactional
    public MomentOfResistanceTestResponseDTO create(
            MomentOfResistanceTestRequestDTO dto) {

        MomentOfResistanceTest entity = new MomentOfResistanceTest();

        entity.setBatchNumber(dto.getBatchNumber());
        entity.setSleeperType(dto.getSleeperType());
        entity.setCastingDate(dto.getCastingDate());
        entity.setDateOfTesting(dto.getDateOfTesting());
        entity.setBenchNumber(dto.getBenchNumber());
        entity.setSleeperNo(dto.getSleeperNo());

        Optional<MomentOfResistance> moment = momentOfResistanceRepository.findById(dto.getMonmentOfResistanceId());
        MomentOfResistance mr = null;
        if(moment.isPresent()){
           mr= moment.get();
        }

        mr.setStatus("COMPLETED");

        // A rejected lot is returned to the vendor and is not tested further
        lotService.assertNotRejected(mr.getProductionDeclarationId());
        entity.setMomentOfResistanceId(mr.getId());

        entity.setVendorCode(dto.getVendorCode());
        entity.setPlantId(dto.getPlantId());
        entity.setShift(dto.getShift());
        entity.setTestResult(dto.getTestResult());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());

        List<MomentOfResistanceDetail> details = dto.getDetails().stream()
                .map(d -> {
                    MomentOfResistanceDetail detail = new MomentOfResistanceDetail();
                    detail.setDataType(d.getDataType());
                    detail.setCt(d.getCt());
                    detail.setCb(d.getCb());
                    detail.setRs1(d.getRs1());
                    detail.setRs2(d.getRs2());
                    detail.setMrTest(entity);
                    return detail;
                }).collect(Collectors.toList());

        entity.setDetails(details);

        momentOfResistanceRepository.save(mr);
        MomentOfResistanceTest saved = repository.save(entity);

        MomentOfResistanceTestResponseDTO response = mapToResponse(saved);
        rejectLotOnDoubleTestingFailure(saved, mr, dto.getCreatedBy(), response);
        return response;
    }

    /* ================= GET BY ID ================= */

    @Override
    public MomentOfResistanceTestResponseDTO getById(Long id) {

        MomentOfResistanceTest entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "MR Test not found")
                ));

        return mapToResponse(entity);
    }

    /* ================= UPDATE ================= */

    @Override
    @Transactional
    public MomentOfResistanceTestResponseDTO update(
            Long id,
            MomentOfResistanceTestRequestDTO dto) {

        MomentOfResistanceTest entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "MR Test not found")
                ));

        // A result that rejected its lot is the record of that rejection and cannot be changed
        assertNotRejectingTest(id);

        // The declaration stored when the result was created (the id sent on update is not reliable);
        // results saved before it was stored have none and are updated as before
        MomentOfResistance declaration = entity.getMomentOfResistanceId() != null
                ? momentOfResistanceRepository.findById(entity.getMomentOfResistanceId()).orElse(null)
                : null;
        if (declaration != null) {
            lotService.assertNotRejected(declaration.getProductionDeclarationId());
        }

        entity.setBatchNumber(dto.getBatchNumber());
        entity.setSleeperType(dto.getSleeperType());
        entity.setCastingDate(dto.getCastingDate());
        entity.setDateOfTesting(dto.getDateOfTesting());
        entity.setBenchNumber(dto.getBenchNumber());
        entity.setSleeperNo(dto.getSleeperNo());

        entity.setVendorCode(dto.getVendorCode());
        entity.setPlantId(dto.getPlantId());
        entity.setShift(dto.getShift());
        entity.setTestResult(dto.getTestResult());

        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());


        entity.getDetails().clear();

        List<MomentOfResistanceDetail> details = dto.getDetails().stream()
                .map(d -> {
                    MomentOfResistanceDetail detail = new MomentOfResistanceDetail();
                    detail.setDataType(d.getDataType());
                    detail.setCt(d.getCt());
                    detail.setCb(d.getCb());
                    detail.setRs1(d.getRs1());
                    detail.setRs2(d.getRs2());
                    detail.setMrTest(entity);
                    return detail;
                }).collect(Collectors.toList());

        entity.getDetails().addAll(details);

        MomentOfResistanceTest updated = repository.save(entity);

        MomentOfResistanceTestResponseDTO response = mapToResponse(updated);
        if (declaration != null) {
            Long userId = dto.getUpdatedBy() != null ? dto.getUpdatedBy() : dto.getCreatedBy();
            rejectLotOnDoubleTestingFailure(updated, declaration, userId, response);
        }
        return response;
    }

    /* ================= GET ALL ================= */

    @Override
    public List<MomentOfResistanceTestResponseDTO> getAll() {

        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /* ================= DELETE ================= */

    @Override
    public void delete(Long id) {

        MomentOfResistanceTest entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "MR Test not found")
                ));

        // A result that rejected its lot is the record of that rejection and cannot be deleted
        assertNotRejectingTest(id);

        repository.delete(entity);

    }

    /* ================= MAPPER ================= */

    private MomentOfResistanceTestResponseDTO mapToResponse(
            MomentOfResistanceTest entity) {

        MomentOfResistanceTestResponseDTO dto =
                new MomentOfResistanceTestResponseDTO();

        dto.setId(entity.getId());
        dto.setBatchNumber(entity.getBatchNumber());
        dto.setSleeperType(entity.getSleeperType());
        dto.setCastingDate(entity.getCastingDate());
        dto.setDateOfTesting(entity.getDateOfTesting());
        dto.setBenchNumber(entity.getBenchNumber());
        dto.setSleeperNo(entity.getSleeperNo());

        dto.setVendorCode(entity.getVendorCode());
        dto.setPlantId(entity.getPlantId());
        dto.setShift(entity.getShift());
        dto.setTestResult(entity.getTestResult());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());

        List<MomentOfResistanceDetailResponseDTO> details =
                entity.getDetails().stream().map(d -> {
                    MomentOfResistanceDetailResponseDTO res =
                            new MomentOfResistanceDetailResponseDTO();
                    res.setId(d.getId());
                    res.setDataType(d.getDataType());
                    res.setCt(d.getCt());
                    res.setCb(d.getCb());
                    res.setRs1(d.getRs1());
                    res.setRs2(d.getRs2());
                    return res;
                }).collect(Collectors.toList());

        dto.setDetails(details);

        return dto;
    }

    /* ================= LOT REJECTION (double testing) ================= */

    /**
     * A "Fail" for samples declared for double testing (the retest of 2 other sleepers after a first-test "Retest")
     * rejects the whole lot: returned to vendor, every sleeper rejected. A "Fail" in the first test is left as before.
     */
    private void rejectLotOnDoubleTestingFailure(MomentOfResistanceTest test, MomentOfResistance declaration,
                                                 Long userId, MomentOfResistanceTestResponseDTO response) {
        if (!mrRules.isFail(test.getTestResult()) || !mrRules.isRetest(declaration.getMrTestType())) {
            return;
        }
        if (declaration.getProductionDeclarationId() == null) {
            throw validation("MR test failed in double testing, but the lot of batch " + test.getBatchNumber()
                    + " is not known, so it cannot be rejected. Declare the double testing samples again from the batch.");
        }
        WaterCubeLotService.Rejection rejection = lotService.rejectBatch(
                declaration.getProductionDeclarationId(),
                mrRules.getRejectionReason(),
                LotRejectionTestType.MR,
                test.getId(),
                userId);
        response.setLotRejected(true);
        response.setRejectedBatchNumber(rejection.batchNumber());
        response.setOpenCallNos(rejection.affectedCallNos());
    }

    private void assertNotRejectingTest(Long testId) {
        if (lotService.isRejectingTest(LotRejectionTestType.MR, testId)) {
            throw validation("This MR test failed in double testing and rejected its lot, so it cannot be changed or deleted.");
        }
    }

    private static BusinessException validation(String message) {
        return new BusinessException(new ErrorDetails(
                AppConstant.ERROR_CODE_RESOURCE,
                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                AppConstant.ERROR_TYPE_VALIDATION,
                message));
    }

    @Override
    public List<MomentOfResistanceTestResponseDTO> getRecordsByDate(
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

        List<MomentOfResistanceTest> list =
                repository.findByDate(
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
