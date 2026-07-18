package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.MomentOfResistanceDetailResponseDTO;
import com.sarthi.Sleeper.dto.MomentOfResistanceTestRequestDTO;
import com.sarthi.Sleeper.dto.MomentOfResistanceTestResponseDTO;
import com.sarthi.Sleeper.entity.MomentOfResistance;
import com.sarthi.Sleeper.entity.MomentOfResistanceDetail;
import com.sarthi.Sleeper.entity.MomentOfResistanceTest;
import com.sarthi.Sleeper.repository.MomentOfResistanceRepository;
import com.sarthi.Sleeper.repository.MomentOfResistanceTestRepository;
import com.sarthi.Sleeper.service.MomentOfResistanceTestService;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    /* ================= CREATE ================= */

    @Override
    public MomentOfResistanceTestResponseDTO create(
            MomentOfResistanceTestRequestDTO dto) {

        MomentOfResistanceTest entity = new MomentOfResistanceTest();

        entity.setBatchNumber(dto.getBatchNumber());
        entity.setSleeperType(dto.getSleeperType());
        entity.setCastingDate(dto.getCastingDate());

        Optional<MomentOfResistance> moment = momentOfResistanceRepository.findById(dto.getMonmentOfResistanceId());
        MomentOfResistance mr = null;
        if(moment.isPresent()){
           mr= moment.get();
        }

        mr.setStatus("COMPLETED");

        entity.setVendorCode(dto.getVendorCode());
        entity.setPlantId(dto.getPlantId());
        entity.setShift(dto.getShift());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());

        List<MomentOfResistanceDetail> details = dto.getDetails().stream()
                .map(d -> {
                    MomentOfResistanceDetail detail = new MomentOfResistanceDetail();
                    detail.setDataType(d.getDataType());
                    detail.setCt(d.getCt());
                    detail.setCb(d.getCb());
                    detail.setRs(d.getRs());
                    detail.setMrTest(entity);
                    return detail;
                }).collect(Collectors.toList());

        entity.setDetails(details);

        momentOfResistanceRepository.save(mr);
        MomentOfResistanceTest saved = repository.save(entity);

        return mapToResponse(saved);
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

        entity.setBatchNumber(dto.getBatchNumber());
        entity.setSleeperType(dto.getSleeperType());
        entity.setCastingDate(dto.getCastingDate());

        entity.setVendorCode(dto.getVendorCode());
        entity.setPlantId(dto.getPlantId());
        entity.setShift(dto.getShift());

        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());


        entity.getDetails().clear();

        List<MomentOfResistanceDetail> details = dto.getDetails().stream()
                .map(d -> {
                    MomentOfResistanceDetail detail = new MomentOfResistanceDetail();
                    detail.setDataType(d.getDataType());
                    detail.setCt(d.getCt());
                    detail.setCb(d.getCb());
                    detail.setRs(d.getRs());
                    detail.setMrTest(entity);
                    return detail;
                }).collect(Collectors.toList());

        entity.getDetails().addAll(details);

        MomentOfResistanceTest updated = repository.save(entity);

        return mapToResponse(updated);
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

        dto.setVendorCode(entity.getVendorCode());
        dto.setPlantId(entity.getPlantId());
        dto.setShift(entity.getShift());

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
                    res.setRs(d.getRs());
                    return res;
                }).collect(Collectors.toList());

        dto.setDetails(details);

        return dto;
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
            // Shift C IST: 10 PM → 6 AM next day
            // DB stores LocalDateTime.now() in UTC (serverTimezone=UTC)
            // IST 22:00 = UTC 16:30 | IST 06:00 next day = UTC 00:30 next day
            startOfDay = selectedDate.atTime(16, 30, 0);
            endOfDay = selectedDate.plusDays(1).atTime(0, 30, 0);
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
