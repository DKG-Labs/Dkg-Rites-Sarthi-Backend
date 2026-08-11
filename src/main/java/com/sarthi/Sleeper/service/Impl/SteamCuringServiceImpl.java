package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.SteamCuring.SteamCuringManualDto;
import com.sarthi.Sleeper.dto.SteamCuring.SteamCuringRequestDto;
import com.sarthi.Sleeper.dto.SteamCuring.SteamCuringResponseDto;
import com.sarthi.Sleeper.dto.SteamCuring.SteamCuringScadaDto;
import com.sarthi.Sleeper.entity.SteamCuring.SteamCuring;
import com.sarthi.Sleeper.entity.SteamCuring.SteamCuringManual;
import com.sarthi.Sleeper.entity.SteamCuring.SteamCuringScada;
import com.sarthi.Sleeper.repository.SteamCuringManualRepository;
import com.sarthi.Sleeper.repository.SteamCuringRepository;
import com.sarthi.Sleeper.repository.SteamCuringScadaRepository;
import com.sarthi.Sleeper.service.SteamCuringService;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
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
public class SteamCuringServiceImpl implements SteamCuringService {
    @Autowired
    private SteamCuringRepository steamCuringRepository;
    @Autowired
    private SteamCuringScadaRepository steamCuringScadaRepository;
    @Autowired
    private SteamCuringManualRepository steamCuringManualRepository;


        @Override
        public SteamCuringResponseDto create(SteamCuringRequestDto dto) {

            SteamCuring entity = new SteamCuring();

            entity.setBatchNo(dto.getBatchNo());
            entity.setChamber(dto.getChamber());
            entity.setGrade(dto.getGrade());

            entity.setShift(dto.getShift());
            entity.setVendorCode(dto.getVendorCode());
            entity.setPlantId(dto.getPlantId());

            if (dto.getEntryDate() != null) {
                entity.setEntryDate(
                        CommonUtils.convertStringToDateObject(dto.getEntryDate()));
            }

            entity.setCreatedBy(dto.getCreatedBy());
            entity.setCreatedDate(LocalDateTime.now());
            entity.setLocation(dto.getLocation());
            entity.setLbcTime(dto.getLbcTime());
            entity.setCuringStage(dto.getCuringStage());
            entity.setTemperature(dto.getTemperature());


            // ===== SCADA =====

            if (dto.getScadaRecords() != null) {

                List<SteamCuringScada> scadaList =
                        dto.getScadaRecords().stream()
                                .map(s -> {

                                    SteamCuringScada sc = new SteamCuringScada();

                                    if (s.getDate() != null) {
                                        sc.setDate(CommonUtils
                                                .convertStringToDateObject(s.getDate()));
                                    }

                                    if (s.getTime() != null) {
                                        sc.setTime(CommonUtils
                                                .convertStringToTimeObject(String.valueOf(s.getTime())));
                                    }

                                    sc.setBatchNo(s.getBatchNo());

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

                                    sc.setTotalSet(s.getTotalSet());
                                    sc.setTotalActual(s.getTotalActual());

                                    sc.setSource("SCADA");

                                    sc.setSteamCuring(entity);

                                    return sc;
                                })
                                .collect(Collectors.toList());

                entity.getScadaRecords().addAll(scadaList);
            }


            // ===== MANUAL =====

            if (dto.getManualRecords() != null) {

                List<SteamCuringManual> manualList =
                        dto.getManualRecords().stream()
                                .map(m -> {

                                    SteamCuringManual mw =
                                            new SteamCuringManual();

                                    mw.setBatchNo(m.getBatchNo());
                                    mw.setChamber(m.getChamber());
                                    mw.setMinTemp(m.getMinTemp());
                                    mw.setMaxTemp(m.getMaxTemp());
                                    mw.setLbcTime(m.getLbcTime());
                                    mw.setCuringStage(m.getCuringStage());
                                    mw.setTemperature(m.getTemperature());

                                    mw.setSource("MANUAL");

                                    mw.setSteamCuring(entity);

                                    return mw;
                                })
                                .collect(Collectors.toList());

                entity.getManualRecords().addAll(manualList);
            }


            SteamCuring saved = steamCuringRepository.save(entity);

            return mapToResponse(saved);
        }


        // ================= UPDATE =================

        @Override
        public SteamCuringResponseDto update(Long id,
                                             SteamCuringRequestDto dto) {

            SteamCuring entity = steamCuringRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Steam Curing record not found.")
                    ));
            entity.setBatchNo(dto.getBatchNo());
            entity.setChamber(dto.getChamber());
            entity.setGrade(dto.getGrade());

            entity.setShift(dto.getShift());
            entity.setVendorCode(dto.getVendorCode());
            entity.setPlantId(dto.getPlantId());

            entity.setLocation(dto.getLocation());
            entity.setLbcTime(dto.getLbcTime());
            entity.setCuringStage(dto.getCuringStage());
            entity.setTemperature(dto.getTemperature());

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

                List<SteamCuringScada> scadaList =
                        dto.getScadaRecords().stream()
                                .map(s -> {

                                    SteamCuringScada sc = new SteamCuringScada();

                                    if (s.getDate() != null) {
                                        sc.setDate(CommonUtils
                                                .convertStringToDateObject(s.getDate()));
                                    }

                                    if (s.getTime() != null) {
                                        sc.setTime(CommonUtils
                                                .convertStringToTimeObject(String.valueOf(s.getTime())));
                                    }

                                    sc.setBatchNo(s.getBatchNo());

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

                                    sc.setTotalSet(s.getTotalSet());
                                    sc.setTotalActual(s.getTotalActual());

                                    sc.setSource("SCADA");

                                    sc.setSteamCuring(entity);

                                    return sc;
                                })
                                .collect(Collectors.toList());

                entity.getScadaRecords().addAll(scadaList);
            }


            // ===== MANUAL =====

            if (dto.getManualRecords() != null) {

                List<SteamCuringManual> manualList =
                        dto.getManualRecords().stream()
                                .map(m -> {

                                    SteamCuringManual mw =
                                            new SteamCuringManual();

                                    mw.setBatchNo(m.getBatchNo());
                                    mw.setChamber(m.getChamber());
                                    mw.setMinTemp(m.getMinTemp());
                                    mw.setMaxTemp(m.getMaxTemp());

                                    mw.setSource("MANUAL");

                                    mw.setSteamCuring(entity);

                                    return mw;
                                })
                                .collect(Collectors.toList());

                entity.getManualRecords().addAll(manualList);
            }


            SteamCuring updated = steamCuringRepository.save(entity);

            return mapToResponse(updated);
        }


        // ================= GET =================

        @Override
        public SteamCuringResponseDto getById(Long id) {

            SteamCuring entity = steamCuringRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Steam Curing record not found.")
                    ));
            return mapToResponse(entity);
        }


        @Override
        public List<SteamCuringResponseDto> getAll() {

            return steamCuringRepository.findAll()
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }


        @Override
        public void delete(Long id) {

            SteamCuring entity = steamCuringRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Steam Curing record not found.")
                    ));
            steamCuringRepository.deleteById(entity.getId());
        }


        // ================= RESPONSE MAPPER =================

        private SteamCuringResponseDto mapToResponse(SteamCuring entity) {

            SteamCuringResponseDto dto = new SteamCuringResponseDto();

            dto.setId(entity.getId());
            dto.setBatchNo(entity.getBatchNo());
            dto.setChamber(entity.getChamber());
            dto.setGrade(entity.getGrade());
            dto.setLocation(entity.getLocation());
            dto.setLbcTime(entity.getLbcTime());
            dto.setCuringStage(entity.getCuringStage());
            dto.setTemperature(entity.getTemperature());

            dto.setShift(entity.getShift());
            dto.setVendorCode(entity.getVendorCode());
            dto.setPlantId(entity.getPlantId());

            if (entity.getEntryDate() != null) {
                dto.setEntryDate(
                        CommonUtils.convertDateToString(entity.getEntryDate()));
            }


            // ===== SCADA RESPONSE =====

            List<SteamCuringScadaDto> scadaDtos =
                    entity.getScadaRecords()
                            .stream()
                            .map(sc -> {

                                SteamCuringScadaDto s =
                                        new SteamCuringScadaDto();

                                if (sc.getDate() != null) {
                                    s.setDate(CommonUtils
                                            .convertDateToString(sc.getDate()));
                                }

                                if (sc.getTime() != null) {
                                    s.setTime(sc.getTime());
                                }

                                s.setBatchNo(sc.getBatchNo());

                                s.setCa1Set(sc.getCa1Set());
                                s.setCa1Actual(sc.getCa1Actual());

                                s.setCa2Set(sc.getCa2Set());
                                s.setCa2Actual(sc.getCa2Actual());

                                s.setFaSet(sc.getFaSet());
                                s.setFaActual(sc.getFaActual());

                                s.setCementSet(sc.getCementSet());
                                s.setCementActual(sc.getCementActual());

                                s.setWaterSet(sc.getWaterSet());
                                s.setWaterActual(sc.getWaterActual());

                                s.setAdmixtureSet(sc.getAdmixtureSet());
                                s.setAdmixtureActual(sc.getAdmixtureActual());

                                s.setTotalSet(sc.getTotalSet());
                                s.setTotalActual(sc.getTotalActual());

                                return s;
                            })
                            .collect(Collectors.toList());

            dto.setScadaRecords(scadaDtos);


            // ===== MANUAL RESPONSE =====

            List<SteamCuringManualDto> manualDtos =
                    entity.getManualRecords()
                            .stream()
                            .map(m -> {

                                SteamCuringManualDto md =
                                        new SteamCuringManualDto();

                                md.setBatchNo(m.getBatchNo());
                                md.setChamber(m.getChamber());
                                md.setMinTemp(m.getMinTemp());
                                md.setMaxTemp(m.getMaxTemp());
                                md.setLbcTime(m.getLbcTime());
                                md.setCuringStage(m.getCuringStage());
                                md.setTemperature(m.getTemperature());

                                return md;
                            })
                            .collect(Collectors.toList());

            dto.setManualRecords(manualDtos);


            return dto;
        }


    @Override
    public List<SteamCuringResponseDto> getRecordsByDate(
            String plantId,
            String vendorCode,
            String shift,
            int createdBy,
            String date) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate selectedDate = LocalDate.parse(date, formatter);

        LocalDateTime startOfDay;
        LocalDateTime endOfDay;

        // DB stores LocalDateTime.now() in UTC (serverTimezone=UTC)
        // Shift C IST: 10 PM → 6 AM next day = UTC 16:30 → 00:30 next day
        if ("C".equalsIgnoreCase(shift)) {
            startOfDay = selectedDate.atTime(16, 30, 0);
            endOfDay = selectedDate.plusDays(1).atTime(0, 30, 0);
        } else {
            startOfDay = selectedDate.atStartOfDay();
            endOfDay = selectedDate.atTime(23, 59, 59);
        }

        List<SteamCuring> list = steamCuringRepository.findByDate(
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
                .map(SteamCuring::getId)
                .toList();

        List<SteamCuringScada> scadaList =
                steamCuringScadaRepository.findBySteamIds(ids);

        List<SteamCuringManual> manualList =
                steamCuringManualRepository.findBySteamIds(ids);

        Map<Long, List<SteamCuringScada>> scadaMap =
                scadaList.stream()
                        .collect(Collectors.groupingBy(
                                s -> s.getSteamCuring().getId()
                        ));

        Map<Long, List<SteamCuringManual>> manualMap =
                manualList.stream()
                        .collect(Collectors.groupingBy(
                                m -> m.getSteamCuring().getId()
                        ));


        for (SteamCuring s : list) {
            s.setScadaRecords(
                    scadaMap.getOrDefault(s.getId(), new ArrayList<>())
            );
            s.setManualRecords(
                    manualMap.getOrDefault(s.getId(), new ArrayList<>())
            );
        }

        return list.stream()
                .map(this::mapToResponse)
                .toList();
    }

}
