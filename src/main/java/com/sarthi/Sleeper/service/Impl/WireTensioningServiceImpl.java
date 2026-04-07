package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.WireTensioningDtos.WireTensioningManualDto;
import com.sarthi.Sleeper.dto.WireTensioningDtos.WireTensioningRequestDto;
import com.sarthi.Sleeper.dto.WireTensioningDtos.WireTensioningResponseDto;
import com.sarthi.Sleeper.dto.WireTensioningDtos.WireTensioningScadaDto;
import com.sarthi.Sleeper.entity.WireTensioning.WireTensioning;
import com.sarthi.Sleeper.entity.WireTensioning.WireTensioningManual;
import com.sarthi.Sleeper.entity.WireTensioning.WireTensioningScada;
import com.sarthi.Sleeper.repository.WireTensioningRepository;
import com.sarthi.Sleeper.service.wireTensioningService;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WireTensioningServiceImpl implements wireTensioningService {
    @Autowired
    private WireTensioningRepository repository;


    // ================= CREATE =================

    @Override
    public WireTensioningResponseDto create(WireTensioningRequestDto dto) {

        WireTensioning entity = new WireTensioning();

        entity.setBatchNo(dto.getBatchNo());
        entity.setSleeperType(dto.getSleeperType());
        entity.setWiresPerSleeper(dto.getWiresPerSleeper());
        entity.setTargetLoadKn(dto.getTargetLoadKn());

        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());

        entity.setVendorCode(dto.getVendorCode());
        entity.setPlantId(dto.getPlantId());
        entity.setShift(dto.getShift());

        // ===== SCADA =====

        if (dto.getScadaRecords() != null) {

            List<WireTensioningScada> scadaList =
                    dto.getScadaRecords().stream()
                            .map(s -> {

                                WireTensioningScada sc =
                                        new WireTensioningScada();

                                if (s.getPlcTime() != null) {
                                    sc.setPlcTime(
                                            CommonUtils.convertStringToTimeObject(
                                                    s.getPlcTime()));
                                }

                                sc.setBenchNo(s.getBenchNo());
                                sc.setWireLength(s.getWireLength());
                                sc.setCrossSection(s.getCrossSection());
                                sc.setYoungsModulus(s.getYoungsModulus());
                                sc.setMeasuredElongation(s.getMeasuredElongation());
                                sc.setForceElongation(s.getForceElongation());
                                sc.setTotalLoad(s.getTotalLoad());
                                sc.setFinalLoad(s.getFinalLoad());

                                sc.setSource("SCADA");
                                sc.setWireTensioning(entity);

                                return sc;

                            })
                            .collect(Collectors.toList());

            entity.getScadaRecords().addAll(scadaList);
        }


        // ===== MANUAL =====

        if (dto.getManualRecords() != null) {

            List<WireTensioningManual> manualList =
                    dto.getManualRecords().stream()
                            .map(m -> {

                                WireTensioningManual mw =
                                        new WireTensioningManual();

                                mw.setBatchNo(m.getBatchNo());
                                mw.setBenchNo(m.getBenchNo());

                                if (m.getTime() != null) {
                                    mw.setTime(
                                            CommonUtils.convertStringToTimeObject(
                                                    m.getTime()));
                                }

                                mw.setWireLength(m.getWireLength());
                                mw.setCrossSection(m.getCrossSection());
                                mw.setYoungsModulus(m.getYoungsModulus());
                                mw.setMeasuredElongation(m.getMeasuredElongation());
                                mw.setForceElongation(m.getForceElongation());
                                mw.setTotalLoad(m.getTotalLoad());
                                mw.setFinalLoad(m.getFinalLoad());

                                mw.setSource("MANUAL");
                                mw.setWireTensioning(entity);

                                return mw;

                            })
                            .collect(Collectors.toList());

            entity.getManualRecords().addAll(manualList);
        }


        WireTensioning saved = repository.save(entity);

        return mapToResponse(saved);
    }


    // ================= UPDATE =================

    @Override
    public WireTensioningResponseDto update(Long id,
                                            WireTensioningRequestDto dto) {

        WireTensioning entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Wire Tensioning record not found.")
                ));
        entity.setBatchNo(dto.getBatchNo());
        entity.setSleeperType(dto.getSleeperType());
        entity.setWiresPerSleeper(dto.getWiresPerSleeper());
        entity.setTargetLoadKn(dto.getTargetLoadKn());

        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());


        entity.getScadaRecords().clear();
        entity.getManualRecords().clear();

        entity.setVendorCode(dto.getVendorCode());
        entity.setPlantId(dto.getPlantId());
        entity.setShift(dto.getShift());

        // ===== SCADA =====

        if (dto.getScadaRecords() != null) {

            List<WireTensioningScada> scadaList =
                    dto.getScadaRecords().stream()
                            .map(s -> {

                                WireTensioningScada sc =
                                        new WireTensioningScada();

                                if (s.getPlcTime() != null) {
                                    sc.setPlcTime(
                                            CommonUtils.convertStringToTimeObject(
                                                    s.getPlcTime()));
                                }

                                sc.setBenchNo(s.getBenchNo());
                                sc.setWireLength(s.getWireLength());
                                sc.setCrossSection(s.getCrossSection());
                                sc.setYoungsModulus(s.getYoungsModulus());
                                sc.setMeasuredElongation(s.getMeasuredElongation());
                                sc.setForceElongation(s.getForceElongation());
                                sc.setTotalLoad(s.getTotalLoad());
                                sc.setFinalLoad(s.getFinalLoad());

                                sc.setSource("SCADA");
                                sc.setWireTensioning(entity);

                                return sc;

                            })
                            .collect(Collectors.toList());

            entity.getScadaRecords().addAll(scadaList);
        }


        // ===== MANUAL =====

        if (dto.getManualRecords() != null) {

            List<WireTensioningManual> manualList =
                    dto.getManualRecords().stream()
                            .map(m -> {

                                WireTensioningManual mw =
                                        new WireTensioningManual();

                                mw.setBatchNo(m.getBatchNo());
                                mw.setBenchNo(m.getBenchNo());

                                if (m.getTime() != null) {
                                    mw.setTime(
                                            CommonUtils.convertStringToTimeObject(
                                                    m.getTime()));
                                }

                                mw.setWireLength(m.getWireLength());
                                mw.setCrossSection(m.getCrossSection());
                                mw.setYoungsModulus(m.getYoungsModulus());
                                mw.setMeasuredElongation(m.getMeasuredElongation());
                                mw.setForceElongation(m.getForceElongation());
                                mw.setTotalLoad(m.getTotalLoad());
                                mw.setFinalLoad(m.getFinalLoad());

                                mw.setSource("MANUAL");
                                mw.setWireTensioning(entity);

                                return mw;

                            })
                            .collect(Collectors.toList());

            entity.getManualRecords().addAll(manualList);
        }


        WireTensioning updated = repository.save(entity);

        return mapToResponse(updated);
    }


    // ================= GET =================

    @Override
    public WireTensioningResponseDto getById(Long id) {

        WireTensioning entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Wire Tensioning record not found.")
                ));
        return mapToResponse(entity);
    }


    @Override
    public List<WireTensioningResponseDto> getAll() {

        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void delete(Long id) {

        WireTensioning entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Wire Tensioning record not found.")
                ));
        repository.deleteById(entity.getId());
    }


    // ================= MAPPER =================

    private WireTensioningResponseDto mapToResponse(
            WireTensioning entity) {

        WireTensioningResponseDto dto =
                new WireTensioningResponseDto();

        dto.setId(entity.getId());
        dto.setBatchNo(entity.getBatchNo());
        dto.setSleeperType(entity.getSleeperType());
        dto.setWiresPerSleeper(entity.getWiresPerSleeper());
        dto.setTargetLoadKn(entity.getTargetLoadKn());

        dto.setShift(entity.getShift());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());

        // ================= SCADA =================

        if (entity.getScadaRecords() != null) {

            List<WireTensioningScadaDto> scadaDtos =
                    entity.getScadaRecords()
                            .stream()
                            .map(s -> {

                                WireTensioningScadaDto sd =
                                        new WireTensioningScadaDto();

                                sd.setId(s.getId());

                                if (s.getPlcTime() != null) {
                                    sd.setPlcTime(
                                            s.getPlcTime().toString());
                                }

                                sd.setBenchNo(s.getBenchNo());
                                sd.setWireLength(s.getWireLength());
                                sd.setCrossSection(s.getCrossSection());
                                sd.setYoungsModulus(s.getYoungsModulus());
                                sd.setMeasuredElongation(s.getMeasuredElongation());
                                sd.setForceElongation(s.getForceElongation());
                                sd.setTotalLoad(s.getTotalLoad());
                                sd.setFinalLoad(s.getFinalLoad());

                                return sd;

                            })
                            .collect(Collectors.toList());

            dto.setScadaRecords(scadaDtos);
        }


        // ================= MANUAL =================

        if (entity.getManualRecords() != null) {

            List<WireTensioningManualDto> manualDtos =
                    entity.getManualRecords()
                            .stream()
                            .map(m -> {

                                WireTensioningManualDto md =
                                        new WireTensioningManualDto();

                                md.setId(m.getId());
                                md.setBatchNo(m.getBatchNo());
                                md.setBenchNo(m.getBenchNo());

                                if (m.getTime() != null) {
                                    md.setTime(m.getTime().toString());
                                }

                                md.setWireLength(m.getWireLength());
                                md.setCrossSection(m.getCrossSection());
                                md.setYoungsModulus(m.getYoungsModulus());
                                md.setMeasuredElongation(m.getMeasuredElongation());
                                md.setForceElongation(m.getForceElongation());
                                md.setTotalLoad(m.getTotalLoad());
                                md.setFinalLoad(m.getFinalLoad());

                                return md;

                            })
                            .collect(Collectors.toList());

            dto.setManualRecords(manualDtos);
        }


        return dto;
    }

    @Override
    public List<WireTensioningResponseDto> getRecordsByDate(
            String plantId,
            String vendorCode,
            String shift,
            int createdBy,
            String date) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate selectedDate = LocalDate.parse(date, formatter);

        LocalDateTime startOfDay = selectedDate.atStartOfDay();
        LocalDateTime endOfDay = selectedDate.atTime(23, 59, 59);

        List<WireTensioning> list = repository.findByDate(
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