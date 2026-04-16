package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.CompactionDtos.CompactionManualDto;
import com.sarthi.Sleeper.dto.CompactionDtos.CompactionRequestDto;
import com.sarthi.Sleeper.dto.CompactionDtos.CompactionResponseDto;
import com.sarthi.Sleeper.dto.CompactionDtos.CompactionScadaDto;
import com.sarthi.Sleeper.entity.Compaction.Compaction;
import com.sarthi.Sleeper.entity.Compaction.CompactionManual;
import com.sarthi.Sleeper.entity.Compaction.CompactionScada;
import com.sarthi.Sleeper.repository.CompactionManualRepository;
import com.sarthi.Sleeper.repository.CompactionRepository;
import com.sarthi.Sleeper.repository.CompactionScadaRepository;
import com.sarthi.Sleeper.service.CompactionService;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CompactionServiceImpl implements CompactionService {
    @Autowired
    private CompactionRepository compactionRepository;
    @Autowired
    private CompactionScadaRepository compactionScadaRepository;
    @Autowired
    private CompactionManualRepository compactionManualRepository;

        // ================= CREATE =================

        @Override
        public CompactionResponseDto create(CompactionRequestDto dto) {

            Compaction entity = new Compaction();

            entity.setBatchNo(dto.getBatchNo());
            entity.setSleeperType(dto.getSleeperType());

            if (dto.getEntryDate() != null) {
                entity.setEntryDate(
                        CommonUtils.convertStringToDateObject(dto.getEntryDate()));
            }

            entity.setCreatedBy(dto.getCreatedBy());
            entity.setCreatedDate(LocalDateTime.now());

            entity.setTime(dto.getTime());
            entity.setLocation(dto.getLocation());

            entity.setVendorCode(dto.getVendorCode());
            entity.setShift(dto.getShift());
            entity.setPlantId(dto.getPlantId());

            // ===== SCADA =====

            if (dto.getScadaRecords() != null) {

                List<CompactionScada> scadaList =
                        dto.getScadaRecords().stream()
                                .map(s -> {

                                    CompactionScada sc = new CompactionScada();

                                    if (s.getTime() != null) {
                                        sc.setTime(
                                                CommonUtils.convertStringToTimeObject(
                                                        s.getTime()));
                                    }

                                    sc.setBenchNo(s.getBenchNo());
                                    sc.setV1V4Rpm(s.getV1V4Rpm());
                                    sc.setDuration(s.getDuration());

                                    sc.setSource("SCADA");
                                    sc.setCompaction(entity);

                                    return sc;

                                }).collect(Collectors.toList());

                entity.getScadaRecords().addAll(scadaList);
            }


            // ===== MANUAL =====

            if (dto.getManualRecords() != null) {

                List<CompactionManual> manualList =
                        dto.getManualRecords().stream()
                                .map(m -> {

                                    CompactionManual mw = new CompactionManual();

                                    mw.setBenchNo(m.getBenchNo());
                                    mw.setMinRpm(m.getMinRpm());
                                    mw.setMaxRpm(m.getMaxRpm());

                                    mw.setSource("MANUAL");
                                    mw.setCompaction(entity);

                                    return mw;

                                }).collect(Collectors.toList());

                entity.getManualRecords().addAll(manualList);
            }


            Compaction saved = compactionRepository.save(entity);

            return mapToResponse(saved);
        }


        // ================= UPDATE =================

        @Override
        public CompactionResponseDto update(Long id,
                                            CompactionRequestDto dto) {

            Compaction entity = compactionRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Compaction record not found.")
                    ));

            entity.setBatchNo(dto.getBatchNo());
            entity.setSleeperType(dto.getSleeperType());



            entity.setTime(dto.getTime());
            entity.setLocation(dto.getLocation());

            entity.setVendorCode(dto.getVendorCode());
            entity.setShift(dto.getShift());
            entity.setPlantId(dto.getPlantId());

            if (dto.getEntryDate() != null) {
                entity.setEntryDate(
                        CommonUtils.convertStringToDateObject(dto.getEntryDate()));
            }

            entity.setUpdatedBy(dto.getUpdatedBy());
            entity.setUpdatedDate(LocalDateTime.now());


            entity.getScadaRecords().clear();
            entity.getManualRecords().clear();


            // ===== SCADA =====

            if (dto.getScadaRecords() != null) {

                List<CompactionScada> scadaList =
                        dto.getScadaRecords().stream()
                                .map(s -> {

                                    CompactionScada sc = new CompactionScada();

                                    if (s.getTime() != null) {
                                        sc.setTime(
                                                CommonUtils.convertStringToTimeObject(
                                                        s.getTime()));
                                    }

                                    sc.setBenchNo(s.getBenchNo());
                                    sc.setV1V4Rpm(s.getV1V4Rpm());
                                    sc.setDuration(s.getDuration());

                                    sc.setSource("SCADA");
                                    sc.setCompaction(entity);

                                    return sc;

                                }).collect(Collectors.toList());

                entity.getScadaRecords().addAll(scadaList);
            }


            // ===== MANUAL =====

            if (dto.getManualRecords() != null) {

                List<CompactionManual> manualList =
                        dto.getManualRecords().stream()
                                .map(m -> {

                                    CompactionManual mw = new CompactionManual();

                                    mw.setBenchNo(m.getBenchNo());
                                    mw.setMinRpm(m.getMinRpm());
                                    mw.setMaxRpm(m.getMaxRpm());

                                    mw.setSource("MANUAL");
                                    mw.setCompaction(entity);

                                    return mw;

                                }).collect(Collectors.toList());

                entity.getManualRecords().addAll(manualList);
            }


            Compaction updated = compactionRepository.save(entity);

            return mapToResponse(updated);
        }


        // ================= GET =================

        @Override
        public CompactionResponseDto getById(Long id) {

            Compaction entity = compactionRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Compaction record not found.")
                    ));
            return mapToResponse(entity);
        }


        @Override
        public List<CompactionResponseDto> getAll() {

            return compactionRepository.findAll()
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }


        @Override
        public void delete(Long id) {
            Compaction entity = compactionRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Compaction record not found.")
                    ));
            compactionRepository.deleteById(entity.getId());
        }


        // ================= MAPPER =================

        private CompactionResponseDto mapToResponse(
                Compaction entity) {

            CompactionResponseDto dto = new CompactionResponseDto();

            dto.setId(entity.getId());
            dto.setBatchNo(entity.getBatchNo());
            dto.setSleeperType(entity.getSleeperType());


            dto.setTime(entity.getTime());
            dto.setLocation(entity.getLocation());

            dto.setVendorCode(entity.getVendorCode());
            dto.setShift(entity.getShift());
            dto.setPlantId(entity.getPlantId());

            if (entity.getEntryDate() != null) {
                dto.setEntryDate(
                        CommonUtils.convertDateToString(entity.getEntryDate()));
            }


            // ===== SCADA =====

            if (entity.getScadaRecords() != null) {

                List<CompactionScadaDto> scadaDtos =
                        entity.getScadaRecords().stream()
                                .map(s -> {

                                    CompactionScadaDto sd =
                                            new CompactionScadaDto();

                                    sd.setId(s.getId());

                                    if (s.getTime() != null) {
                                        sd.setTime(s.getTime().toString());
                                    }

                                    sd.setBenchNo(s.getBenchNo());
                                    sd.setV1V4Rpm(s.getV1V4Rpm());
                                    sd.setDuration(s.getDuration());

                                    return sd;

                                }).collect(Collectors.toList());

                dto.setScadaRecords(scadaDtos);
            }


            // ===== MANUAL =====

            if (entity.getManualRecords() != null) {

                List<CompactionManualDto> manualDtos =
                        entity.getManualRecords().stream()
                                .map(m -> {

                                    CompactionManualDto md =
                                            new CompactionManualDto();

                                    md.setId(m.getId());
                                    md.setBenchNo(m.getBenchNo());
                                    md.setMinRpm(m.getMinRpm());
                                    md.setMaxRpm(m.getMaxRpm());

                                    return md;

                                }).collect(Collectors.toList());

                dto.setManualRecords(manualDtos);
            }


            return dto;
        }
    @Override
    public List<CompactionResponseDto> getRecordsByDate(
            String plantId,
            String vendorCode,
            String shift,
            int createdBy,
            String date) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate selectedDate = LocalDate.parse(date, formatter);

        LocalDateTime startOfDay = selectedDate.atStartOfDay();
        LocalDateTime endOfDay = selectedDate.atTime(23, 59, 59);

        List<Compaction> list = compactionRepository.findByDate(
                plantId.trim(),
                vendorCode.trim(),
                shift.trim(),
                createdBy,
                startOfDay,
                endOfDay
        );

        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> ids = list.stream()
                .map(Compaction::getId)
                .toList();


        List<CompactionScada> scadaList =
                compactionScadaRepository.findByCompactionIds(ids);

        List<CompactionManual> manualList =
                compactionManualRepository.findByCompactionIds(ids);


        Map<Long, List<CompactionScada>> scadaMap =
                scadaList.stream()
                        .collect(Collectors.groupingBy(
                                s -> s.getCompaction().getId()
                        ));

        Map<Long, List<CompactionManual>> manualMap =
                manualList.stream()
                        .collect(Collectors.groupingBy(
                                m -> m.getCompaction().getId()
                        ));


        for (Compaction c : list) {
            c.setScadaRecords(
                    scadaMap.getOrDefault(c.getId(), new ArrayList<>())
            );
            c.setManualRecords(
                    manualMap.getOrDefault(c.getId(), new ArrayList<>())
            );
        }


        return list.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private CompactionResponseDto mapToResp(Compaction entity) {

        CompactionResponseDto dto = new CompactionResponseDto();

        dto.setId(entity.getId());
        dto.setBatchNo(entity.getBatchNo());
        dto.setSleeperType(entity.getSleeperType());

        dto.setLocation(entity.getLocation());
        dto.setTime(entity.getTime());
        dto.setVendorCode(entity.getVendorCode());
        dto.setShift(entity.getShift());
        dto.setPlantId(entity.getPlantId());

        if (entity.getEntryDate() != null) {
            dto.setEntryDate(
                    CommonUtils.convertDateToString(entity.getEntryDate()));
        }

        //  REMOVE CHILD DATA
        dto.setScadaRecords(null);
        dto.setManualRecords(null);

        return dto;
    }

}
