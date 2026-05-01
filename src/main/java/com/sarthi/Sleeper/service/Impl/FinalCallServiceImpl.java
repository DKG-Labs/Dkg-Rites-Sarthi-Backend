package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.FinalCalDtos.FinalCallRequestDto;
import com.sarthi.Sleeper.dto.FinalCalDtos.FinalCallResponseDto;
import com.sarthi.Sleeper.dto.FinalCalDtos.RejectedDto;

import com.sarthi.Sleeper.dto.FinalCalDtos.SleeperDto;
import com.sarthi.Sleeper.entity.FInalCall.*;
import com.sarthi.Sleeper.repository.FInalCallRepo.IEBatchSummaryRepository;
import com.sarthi.Sleeper.service.FinalCallService;
import com.sarthi.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinalCallServiceImpl implements FinalCallService {



        private final IEBatchSummaryRepository repository;

        // ================= CREATE =================
        public FinalCallResponseDto create(FinalCallRequestDto dto) {

            IEBatchSummary entity = new IEBatchSummary();

            mapCommonFields(entity, dto);

            entity.setCreatedBy(dto.getCreatedBy());
            entity.setCreatedDate(LocalDateTime.now());

            setChildData(entity, dto);

            IEBatchSummary saved = repository.save(entity);

            return mapToResponse(saved);
        }
        @Transactional
    @Override
    public FinalCallResponseDto update( FinalCallRequestDto dto) {

            IEBatchSummary entity = repository.findByCallNo(dto.getCallNo())
                    .orElseThrow(() -> new RuntimeException("Data not found for callNo: " + dto.getCallNo()));

            //IEBatchSummary entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Batch not found"));

        mapCommonFields(entity, dto);

        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        //  Do NOT replace list reference
        syncGoodSleepers(entity, dto.getGoodSleepers());
        syncEtSleepers(entity, dto.getEtSleepers());
        syncMfSleepers(entity, dto.getMfSleepers());
        syncRejectedSleepers(entity, dto.getRejectedSleepers());
        syncFinalRejections(entity, dto.getFinalRejections());

        IEBatchSummary saved = repository.save(entity);

        return mapToResponse(saved);
    }

    private void syncGoodSleepers(IEBatchSummary entity, List<SleeperDto> dtoList) {

        List<FinalGoodSleepers> existing = entity.getGoodSleepers();

        Map<Long, FinalGoodSleepers> map = existing.stream()
                .collect(Collectors.toMap(FinalGoodSleepers::getSleeperId, e -> e));

        existing.clear();

        if (dtoList != null) {
            for (SleeperDto dto : dtoList) {

                FinalGoodSleepers item = map.get(dto.getSleeperId());

                if (item == null) {
                    item = FinalGoodSleepers.builder()
                            .sleeperId(dto.getSleeperId())
                            .sleeperCode(dto.getSleeperCode())
                            .batch(entity)
                            .build();
                }

                existing.add(item);
            }
        }
    }

    private void syncEtSleepers(IEBatchSummary entity, List<SleeperDto> dtoList) {

        List<FinalCallETSleeper> existing = entity.getEtSleepers();

        Map<Long, FinalCallETSleeper> map = existing.stream()
                .collect(Collectors.toMap(FinalCallETSleeper::getSleeperId, e -> e));

        existing.clear();

        if (dtoList != null) {
            for (SleeperDto dto : dtoList) {

                FinalCallETSleeper item = map.get(dto.getSleeperId());

                if (item == null) {
                    item = FinalCallETSleeper.builder()
                            .sleeperId(dto.getSleeperId())
                            .sleeperCode(dto.getSleeperCode())
                            .batch(entity)
                            .build();
                }

                existing.add(item);
            }
        }
    }
    private void syncMfSleepers(IEBatchSummary entity, List<SleeperDto> dtoList) {

        List<FinalMFSleeper> existing = entity.getMfSleepers();

        Map<Long, FinalMFSleeper> map = existing.stream()
                .collect(Collectors.toMap(FinalMFSleeper::getSleeperId, e -> e));

        existing.clear();

        if (dtoList != null) {
            for (SleeperDto dto : dtoList) {

                FinalMFSleeper item = map.get(dto.getSleeperId());

                if (item == null) {
                    item = FinalMFSleeper.builder()
                            .sleeperId(dto.getSleeperId())
                            .sleeperCode(dto.getSleeperCode())
                            .batch(entity)
                            .build();
                }

                existing.add(item);
            }
        }
    }
    private void syncRejectedSleepers(IEBatchSummary entity, List<RejectedDto> dtoList) {

        List<FinalCallRejectedSleeper> existing = entity.getRejectedSleepers();

        Map<Long, FinalCallRejectedSleeper> map = existing.stream()
                .collect(Collectors.toMap(FinalCallRejectedSleeper::getSleeperId, e -> e));

        existing.clear();

        if (dtoList != null) {
            for (RejectedDto dto : dtoList) {

                FinalCallRejectedSleeper item = map.get(dto.getSleeperId());

                if (item == null) {
                    item = FinalCallRejectedSleeper.builder()
                            .sleeperId(dto.getSleeperId())
                            .sleeperCode(dto.getSleeperCode())
                            .reason(dto.getReason())
                            .type(dto.getType())
                            .batch(entity)
                            .build();
                } else {
                    item.setReason(dto.getReason());
                    item.setType(dto.getType());
                }

                existing.add(item);
            }
        }
    }
    private void syncFinalRejections(IEBatchSummary entity, List<RejectedDto> dtoList) {

        List<FinalInspectionRejection> existing = entity.getFinalRejections();

        Map<Long, FinalInspectionRejection> map = existing.stream()
                .collect(Collectors.toMap(FinalInspectionRejection::getSleeperId, e -> e));

        existing.clear();

        if (dtoList != null) {
            for (RejectedDto dto : dtoList) {

                FinalInspectionRejection item = map.get(dto.getSleeperId());

                if (item == null) {
                    item = FinalInspectionRejection.builder()
                            .sleeperId(dto.getSleeperId())
                            .sleeperCode(dto.getSleeperCode())
                            .reason(dto.getReason())
                            .batch(entity)
                            .build();
                } else {
                    item.setReason(dto.getReason());
                }

                existing.add(item);
            }
        }
    }


    public List<FinalCallResponseDto> getByCallNo(String callNo) {

        return repository.findByCallNo(callNo)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void mapCommonFields(IEBatchSummary entity, FinalCallRequestDto dto) {

        entity.setBatchNo(dto.getBatchNo());
        entity.setCallNo(dto.getCallNo());

        if (dto.getDateCasted() != null) {
            entity.setDateCasted(CommonUtils.convertStringToDateObject(dto.getDateCasted()));
        }

        entity.setCasted(dto.getCasted());
        entity.setOfferedPrev(dto.getOfferedPrev());
        entity.setOfferedNow(dto.getOfferedNow());

        entity.setPassed(dto.getPassed());
        entity.setRejected(dto.getRejected());

        entity.setTotalOffered(dto.getTotalOffered());
        entity.setTotalAccepted(dto.getTotalAccepted());
        entity.setTotalRejected(dto.getTotalRejected());

        entity.setShift(dto.getShift());
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
    }

    private void setChildData(IEBatchSummary entity, FinalCallRequestDto dto) {

        entity.setGoodSleepers(new ArrayList<>());
        entity.setRejectedSleepers(new ArrayList<>());
        entity.setEtSleepers(new ArrayList<>());
        entity.setMfSleepers(new ArrayList<>());
        entity.setFinalRejections(new ArrayList<>());

        // GOOD
        if (dto.getGoodSleepers() != null) {
            dto.getGoodSleepers().forEach(s ->
                    entity.getGoodSleepers().add(
                            FinalGoodSleepers.builder()
                                    .sleeperId(s.getSleeperId())
                                    .sleeperCode(s.getSleeperCode())
                                    .batch(entity)
                                    .build()
                    ));
        }

        // REJECTED
        if (dto.getRejectedSleepers() != null) {
            dto.getRejectedSleepers().forEach(s ->
                    entity.getRejectedSleepers().add(
                            FinalCallRejectedSleeper.builder()
                                    .sleeperId(s.getSleeperId())
                                    .sleeperCode(s.getSleeperCode())
                                    .reason(s.getReason())
                                    .type(s.getType())
                                    .batch(entity)
                                    .build()
                    ));
        }

        // ET
        if (dto.getEtSleepers() != null) {
            dto.getEtSleepers().forEach(s ->
                    entity.getEtSleepers().add(
                            FinalCallETSleeper.builder()
                                    .sleeperId(s.getSleeperId())
                                    .sleeperCode(s.getSleeperCode())
                                    .batch(entity)
                                    .build()
                    ));
        }

        // MF
        if (dto.getMfSleepers() != null) {
            dto.getMfSleepers().forEach(s ->
                    entity.getMfSleepers().add(
                            FinalMFSleeper.builder()
                                    .sleeperId(s.getSleeperId())
                                    .sleeperCode(s.getSleeperCode())
                                    .batch(entity)
                                    .build()
                    ));
        }

        // FINAL REJECTION
        if (dto.getFinalRejections() != null) {
            dto.getFinalRejections().forEach(s ->
                    entity.getFinalRejections().add(
                            FinalInspectionRejection.builder()
                                    .sleeperId(s.getSleeperId())
                                    .sleeperCode(s.getSleeperCode())
                                    .reason(s.getReason())
                                    .batch(entity)
                                    .build()
                    ));
        }
    }

    private FinalCallResponseDto mapToResponse(IEBatchSummary e) {

        return FinalCallResponseDto.builder()
                .id(e.getId())
                .batchNo(e.getBatchNo())
                .callNo(e.getCallNo())
                .dateCasted(e.getDateCasted())

                .casted(e.getCasted())
                .offeredPrev(e.getOfferedPrev())
                .offeredNow(e.getOfferedNow())

                .passed(e.getPassed())
                .rejected(e.getRejected())

                .totalOffered(e.getTotalOffered())
                .totalAccepted(e.getTotalAccepted())
                .totalRejected(e.getTotalRejected())

                .shift(e.getShift())
                .plantId(e.getPlantId())
                .vendorCode(e.getVendorCode())

                .goodSleepers(mapSleepers(e.getGoodSleepers()))
                .etSleepers(mapSleepers(e.getEtSleepers()))
                .mfSleepers(mapSleepers(e.getMfSleepers()))
                .rejectedSleepers(mapRejected(e.getRejectedSleepers()))
                .finalRejections(mapRejected(e.getFinalRejections()))

                .build();
    }

    private List<SleeperDto> mapSleepers(List<? extends Object> list) {
        return list.stream().map(obj -> {
            SleeperDto dto = new SleeperDto();
            if (obj instanceof FinalGoodSleepers s) {
                dto.setSleeperId(s.getSleeperId());
                dto.setSleeperCode(s.getSleeperCode());
            } else if (obj instanceof FinalCallETSleeper s) {
                dto.setSleeperId(s.getSleeperId());
                dto.setSleeperCode(s.getSleeperCode());
            } else if (obj instanceof FinalMFSleeper s) {
                dto.setSleeperId(s.getSleeperId());
                dto.setSleeperCode(s.getSleeperCode());
            }
            return dto;
        }).toList();
    }
    private List<RejectedDto> mapRejected(List<? extends Object> list) {
        return list.stream().map(obj -> {
            RejectedDto dto = new RejectedDto();
            if (obj instanceof FinalCallRejectedSleeper s) {
                dto.setSleeperId(s.getSleeperId());
                dto.setSleeperCode(s.getSleeperCode());
                dto.setReason(s.getReason());
                dto.setType(s.getType());
            } else if (obj instanceof FinalInspectionRejection s) {
                dto.setSleeperId(s.getSleeperId());
                dto.setSleeperCode(s.getSleeperCode());
                dto.setReason(s.getReason());
            }
            return dto;
        }).toList();
    }
}
