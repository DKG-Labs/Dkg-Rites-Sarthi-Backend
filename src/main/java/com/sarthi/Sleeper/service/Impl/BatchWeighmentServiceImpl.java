package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.BatchWeighmentDtos.*;
import com.sarthi.Sleeper.entity.BatchWeighment.BatchDetails;
import com.sarthi.Sleeper.entity.BatchWeighment.BatchWeighment;
import com.sarthi.Sleeper.entity.BatchWeighment.ManualWeighment;
import com.sarthi.Sleeper.entity.BatchWeighment.ScadaWeighment;
import com.sarthi.Sleeper.repository.BatchWeighmentRepository;
import com.sarthi.Sleeper.service.BatchWeighmentService;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import org.springframework.stereotype.Service;

import com.sarthi.util.CommonUtils;
import lombok.RequiredArgsConstructor;


import java.time.LocalDate;
import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BatchWeighmentServiceImpl implements BatchWeighmentService {

    private final BatchWeighmentRepository repository;


        @Override
        public BatchWeighmentResponseDto create(BatchWeighmentRequestDto dto) {

            BatchWeighment entity = new BatchWeighment();

            entity.setLineNo(dto.getLineNo());

            if (dto.getEntryDate() != null) {
                entity.setEntryDate(
                        CommonUtils.convertStringToDateObject(dto.getEntryDate()));
            }

            entity.setSandType(dto.getSandType());
            entity.setMoistureSensorStatus(dto.getMoistureSensorStatus());
            entity.setVerifiedBy(dto.getVerifiedBy());
            entity.setRemarks(dto.getRemarks());
            entity.setEntryMode(dto.getEntryMode());

            entity.setCreatedBy(dto.getCreatedBy());
            entity.setCreatedDate(LocalDateTime.now());

            entity.setShift(dto.getShift());
            entity.setVendorCode(dto.getVendorCode());
            entity.setPlantId(dto.getPlantId());

            // ========= Batch Details =========

            if (dto.getBatchDetails() != null) {

                List<BatchDetails> detailsList = dto.getBatchDetails()
                        .stream()
                        .map(d -> {

                            BatchDetails bd = new BatchDetails();

                            bd.setBatchNo(d.getBatchNo());
                            bd.setProportionStatus(d.getProportionStatus());

                            bd.setCa1Ref(d.getCa1Ref());
                            bd.setCa2Ref(d.getCa2Ref());
                            bd.setFaRef(d.getFaRef());
                            bd.setCementRef(d.getCementRef());
                            bd.setWaterRef(d.getWaterRef());
                            bd.setAdmixtureRef(d.getAdmixtureRef());

                            bd.setCa1Set(d.getCa1Set());
                            bd.setCa2Set(d.getCa2Set());
                            bd.setFaSet(d.getFaSet());
                            bd.setCementSet(d.getCementSet());
                            bd.setWaterSet(d.getWaterSet());
                            bd.setAdmixtureSet(d.getAdmixtureSet());

                            bd.setBatchWeighment(entity);

                            return bd;

                        }).toList();

                entity.setBatchDetailsList(detailsList);
            }


            // ========= SCADA =========

            if (dto.getScadaRecords() != null) {

                List<ScadaWeighment> scadaList = dto.getScadaRecords()
                        .stream()
                        .map(s -> {

                            ScadaWeighment sc = new ScadaWeighment();

                            sc.setBatchNo(s.getBatchNo());

                            if (s.getDate() != null) {
                                sc.setDate(CommonUtils.convertStringToDateObject(s.getDate()));
                            }

                            if (s.getTime() != null) {
                                sc.setTime(CommonUtils.convertStringToTimeObject(s.getTime()));
                            }

                            sc.setCa1Set(s.getCa1Set());
                            sc.setCa1Actual(s.getCa1Actual());

                            sc.setCa2Set(s.getCa2Set());
                            sc.setCa2Actual(s.getCa2Actual());

                            sc.setFaSet(s.getFaSet());
                            sc.setFaActual(s.getFaActual());

                            sc.setCementSet(s.getCementSet());
                            sc.setCementActual(s.getCementActual());

                            sc.setWaterSet(s.getWaterSet());
                            sc.setWaterActual(s.getWaterActual());

                            sc.setAdmixtureSet(s.getAdmixtureSet());
                            sc.setAdmixtureActual(s.getAdmixtureActual());

                            sc.setTotal(s.getTotal());
                            sc.setSource("SCADA");

                            sc.setBatchWeighment(entity);

                            return sc;

                        }).toList();

                entity.setScadaRecords(scadaList);
            }


            // ========= MANUAL =========

            if (dto.getManualRecords() != null) {

                List<ManualWeighment> manualList = dto.getManualRecords()
                        .stream()
                        .map(m -> {

                            ManualWeighment mw = new ManualWeighment();

                            mw.setBatchNo(m.getBatchNo());

                            if (m.getDate() != null) {
                                mw.setDate(CommonUtils.convertStringToDateObject(m.getDate()));
                            }

                            if (m.getTime() != null) {
                                mw.setTime(CommonUtils.convertStringToTimeObject(m.getTime()));
                            }

                            mw.setCa1Actual(m.getCa1Actual());
                            mw.setCa2Actual(m.getCa2Actual());
                            mw.setFaActual(m.getFaActual());
                            mw.setCementActual(m.getCementActual());
                            mw.setWaterActual(m.getWaterActual());
                            mw.setAdmixtureActual(m.getAdmixtureActual());

                            mw.setSource("MANUAL");

                            mw.setBatchWeighment(entity);

                            return mw;

                        }).toList();

                entity.setManualRecords(manualList);
            }


            BatchWeighment saved = repository.save(entity);

            return mapToResponse(saved);
        }


        // ================= UPDATE =================

    @Override
    public BatchWeighmentResponseDto update(Long id, BatchWeighmentRequestDto dto) {

        BatchWeighment entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Batch not found.")
                ));


        entity.setLineNo(dto.getLineNo());

        if (dto.getEntryDate() != null) {
            entity.setEntryDate(
                    CommonUtils.convertStringToDateObject(dto.getEntryDate()));
        }

        entity.setSandType(dto.getSandType());
        entity.setMoistureSensorStatus(dto.getMoistureSensorStatus());
        entity.setVerifiedBy(dto.getVerifiedBy());
        entity.setRemarks(dto.getRemarks());
        entity.setEntryMode(dto.getEntryMode());

        entity.setShift(dto.getShift());
        entity.setVendorCode(dto.getVendorCode());
        entity.setPlantId(dto.getPlantId());

        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());


        // Clear old child records
        entity.getBatchDetailsList().clear();
        entity.getScadaRecords().clear();
        entity.getManualRecords().clear();

        // ========= Batch Details =========

        if (dto.getBatchDetails() != null) {

            List<BatchDetails> detailsList = dto.getBatchDetails()
                    .stream()
                    .map(d -> {

                        BatchDetails bd = new BatchDetails();

                        bd.setBatchNo(d.getBatchNo());
                        bd.setProportionStatus(d.getProportionStatus());

                        bd.setCa1Ref(d.getCa1Ref());
                        bd.setCa2Ref(d.getCa2Ref());
                        bd.setFaRef(d.getFaRef());
                        bd.setCementRef(d.getCementRef());
                        bd.setWaterRef(d.getWaterRef());
                        bd.setAdmixtureRef(d.getAdmixtureRef());

                        bd.setCa1Set(d.getCa1Set());
                        bd.setCa2Set(d.getCa2Set());
                        bd.setFaSet(d.getFaSet());
                        bd.setCementSet(d.getCementSet());
                        bd.setWaterSet(d.getWaterSet());
                        bd.setAdmixtureSet(d.getAdmixtureSet());

                        bd.setBatchWeighment(entity);

                        return bd;

                    })
                    .collect(Collectors.toList());

            entity.getBatchDetailsList().addAll(detailsList);
        }


        // ========= SCADA =========

        if (dto.getScadaRecords() != null) {

            List<ScadaWeighment> scadaList = dto.getScadaRecords()
                    .stream()
                    .map(s -> {

                        ScadaWeighment sc = new ScadaWeighment();

                        sc.setBatchNo(s.getBatchNo());

                        if (s.getDate() != null) {
                            sc.setDate(CommonUtils.convertStringToDateObject(s.getDate()));
                        }

                        if (s.getTime() != null) {
                            sc.setTime(CommonUtils.convertStringToTimeObject(s.getTime()));
                        }

                        sc.setCa1Set(s.getCa1Set());
                        sc.setCa1Actual(s.getCa1Actual());

                        sc.setCa2Set(s.getCa2Set());
                        sc.setCa2Actual(s.getCa2Actual());

                        sc.setFaSet(s.getFaSet());
                        sc.setFaActual(s.getFaActual());

                        sc.setCementSet(s.getCementSet());
                        sc.setCementActual(s.getCementActual());

                        sc.setWaterSet(s.getWaterSet());
                        sc.setWaterActual(s.getWaterActual());

                        sc.setAdmixtureSet(s.getAdmixtureSet());
                        sc.setAdmixtureActual(s.getAdmixtureActual());

                        sc.setTotal(s.getTotal());
                        sc.setSource("SCADA");

                        sc.setBatchWeighment(entity);

                        return sc;

                    })
                    .collect(Collectors.toList());

            entity.getScadaRecords().addAll(scadaList);
        }


        // ========= MANUAL =========

        if (dto.getManualRecords() != null) {

            List<ManualWeighment> manualList = dto.getManualRecords()
                    .stream()
                    .map(m -> {

                        ManualWeighment mw = new ManualWeighment();

                        mw.setBatchNo(m.getBatchNo());

                        if (m.getDate() != null) {
                            mw.setDate(CommonUtils.convertStringToDateObject(m.getDate()));
                        }

                        if (m.getTime() != null) {
                            mw.setTime(CommonUtils.convertStringToTimeObject(m.getTime()));
                        }

                        mw.setCa1Actual(m.getCa1Actual());
                        mw.setCa2Actual(m.getCa2Actual());
                        mw.setFaActual(m.getFaActual());
                        mw.setCementActual(m.getCementActual());
                        mw.setWaterActual(m.getWaterActual());
                        mw.setAdmixtureActual(m.getAdmixtureActual());

                        mw.setSource("MANUAL");

                        mw.setBatchWeighment(entity);

                        return mw;

                    })
                    .collect(Collectors.toList());

            entity.getManualRecords().addAll(manualList);
        }


        BatchWeighment updated = repository.save(entity);

        return mapToResponse(updated);
    }


        // ================= GET =================

        @Override
        public BatchWeighmentResponseDto getById(Long id) {

            BatchWeighment entity = repository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Batch not found.")
                    ));
            return mapToResponse(entity);
        }


        @Override
        public List<BatchWeighmentResponseDto> getAll() {

            return repository.findAll()
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }


        @Override
        public void delete(Long id) {
            BatchWeighment entity = repository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Batch not found.")
                    ));
            repository.deleteById(entity.getId());
        }


        // ================= RESPONSE MAPPING =================

    private BatchWeighmentResponseDto mapToResponse(BatchWeighment entity) {

        BatchWeighmentResponseDto dto = new BatchWeighmentResponseDto();

        dto.setId(entity.getId());
        dto.setLineNo(entity.getLineNo());

        if (entity.getEntryDate() != null) {
            dto.setEntryDate(
                    CommonUtils.convertDateToString(entity.getEntryDate()));
        }

        dto.setSandType(entity.getSandType());
        dto.setMoistureSensorStatus(entity.getMoistureSensorStatus());
        dto.setVerifiedBy(entity.getVerifiedBy());
        dto.setRemarks(entity.getRemarks());
        dto.setEntryMode(entity.getEntryMode());

        dto.setVendorCode(entity.getVendorCode());

        dto.setShift(entity.getShift());
        dto.setPlantId(entity.getPlantId());
        // ================= Batch Details =================

        if (entity.getBatchDetailsList() != null) {

            List<BatchDetailsDto> batchDtos =
                    entity.getBatchDetailsList()
                            .stream()
                            .map(b -> {

                                BatchDetailsDto bd = new BatchDetailsDto();

                                bd.setId(b.getId());
                                bd.setBatchNo(b.getBatchNo());
                                bd.setProportionStatus(b.getProportionStatus());

                                bd.setCa1Ref(b.getCa1Ref());
                                bd.setCa2Ref(b.getCa2Ref());
                                bd.setFaRef(b.getFaRef());
                                bd.setCementRef(b.getCementRef());
                                bd.setWaterRef(b.getWaterRef());
                                bd.setAdmixtureRef(b.getAdmixtureRef());

                                bd.setCa1Set(b.getCa1Set());
                                bd.setCa2Set(b.getCa2Set());
                                bd.setFaSet(b.getFaSet());
                                bd.setCementSet(b.getCementSet());
                                bd.setWaterSet(b.getWaterSet());
                                bd.setAdmixtureSet(b.getAdmixtureSet());

                                return bd;

                            }).toList();

            dto.setBatchDetails(batchDtos);
        }


        // ================= SCADA =================

        if (entity.getScadaRecords() != null) {

            List<ScadaWeighmentDto> scadaDtos =
                    entity.getScadaRecords()
                            .stream()
                            .map(s -> {

                                ScadaWeighmentDto sd = new ScadaWeighmentDto();

                                sd.setId(s.getId());
                                sd.setBatchNo(s.getBatchNo());

                                if (s.getDate() != null) {
                                    sd.setDate(
                                            CommonUtils.convertDateToString(s.getDate()));
                                }

                                if (s.getTime() != null) {
                                    sd.setTime(s.getTime().toString());
                                }

                                sd.setCa1Set(s.getCa1Set());
                                sd.setCa1Actual(s.getCa1Actual());

                                sd.setCa2Set(s.getCa2Set());
                                sd.setCa2Actual(s.getCa2Actual());

                                sd.setFaSet(s.getFaSet());
                                sd.setFaActual(s.getFaActual());

                                sd.setCementSet(s.getCementSet());
                                sd.setCementActual(s.getCementActual());

                                sd.setWaterSet(s.getWaterSet());
                                sd.setWaterActual(s.getWaterActual());

                                sd.setAdmixtureSet(s.getAdmixtureSet());
                                sd.setAdmixtureActual(s.getAdmixtureActual());

                                sd.setTotal(s.getTotal());

                                return sd;

                            }).toList();

            dto.setScadaRecords(scadaDtos);
        }


        // ================= MANUAL =================

        if (entity.getManualRecords() != null) {

            List<ManualWeighmentDto> manualDtos =
                    entity.getManualRecords()
                            .stream()
                            .map(m -> {

                                ManualWeighmentDto md = new ManualWeighmentDto();

                                md.setId(m.getId());
                                md.setBatchNo(m.getBatchNo());

                                if (m.getDate() != null) {
                                    md.setDate(
                                            CommonUtils.convertDateToString(m.getDate()));
                                }

                                if (m.getTime() != null) {
                                    md.setTime(m.getTime().toString());
                                }

                                md.setCa1Actual(m.getCa1Actual());
                                md.setCa2Actual(m.getCa2Actual());
                                md.setFaActual(m.getFaActual());
                                md.setCementActual(m.getCementActual());
                                md.setWaterActual(m.getWaterActual());
                                md.setAdmixtureActual(m.getAdmixtureActual());

                                return md;

                            }).toList();

            dto.setManualRecords(manualDtos);
        }

        return dto;
    }
    @Override
    public List<BatchWeighmentResponseDto> getRecordsByDate(
            String plantId,
            String vendorCode,
            String shift,
            int createdBy,
            String date) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate selectedDate = LocalDate.parse(date, formatter);

        LocalDateTime startOfDay = selectedDate.atStartOfDay();
        LocalDateTime endOfDay = selectedDate.atTime(23, 59, 59);

        List<BatchWeighment> list = repository.findByDate(
                plantId.trim(),
                vendorCode.trim(),
                shift.trim(),
                createdBy,
                startOfDay,
                endOfDay
        );

        return list.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

}
