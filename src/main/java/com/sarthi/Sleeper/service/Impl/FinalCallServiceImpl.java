package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.FinalCalDtos.*;

import com.sarthi.Sleeper.entity.FInalCall.*;
import com.sarthi.Sleeper.repository.FInalCallRepo.FinalCallInspectionHeaderRepository;
import com.sarthi.Sleeper.repository.FInalCallRepo.IEBatchSummaryRepository;
import com.sarthi.Sleeper.service.FinalCallService;
import com.sarthi.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarthi.Sleeper.repository.FInalCallRepo.SleeperBatchResultRepository;
import com.sarthi.Sleeper.repository.FInalCallRepo.SleeperFinalResultRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FinalCallServiceImpl implements FinalCallService {

    private final IEBatchSummaryRepository repository;
    private final FinalCallInspectionHeaderRepository finalCallInspectionHeaderRepository;
    private final SleeperFinalResultRepository sleeperFinalResultRepository;
    private final SleeperBatchResultRepository sleeperBatchResultRepository;

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
                            .reason(dto.getReason())
                            .sleeperFinalResultId(dto.getSleeperFinalResultId())
                            .batch(entity)
                            .build();
                } else {
                    item.setReason(dto.getReason());
                    item.setSleeperFinalResultId(dto.getSleeperFinalResultId());
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
                .filter(e -> e.getSleeperId() != null)
                .collect(Collectors.toMap(FinalCallRejectedSleeper::getSleeperId, e -> e, (a, b) -> a));

        existing.clear();

        if (dtoList != null) {
            for (RejectedDto dto : dtoList) {

                FinalCallRejectedSleeper item = dto.getSleeperId() != null ? map.get(dto.getSleeperId()) : null;

                if (item == null) {
                    item = FinalCallRejectedSleeper.builder()
                            .sleeperId(dto.getSleeperId())
                            .sleeperCode(dto.getSleeperCode())
                            .reason(dto.getReason())
                            .type(dto.getType())
                            .sleeperFinalResultId(dto.getSleeperFinalResultId())
                            .batch(entity)
                            .build();
                } else {
                    item.setReason(dto.getReason());
                    item.setType(dto.getType());
                    item.setSleeperFinalResultId(dto.getSleeperFinalResultId());
                }

                existing.add(item);
            }
        }
    }
    private void syncFinalRejections(IEBatchSummary entity, List<RejectedDto> dtoList) {

        List<FinalInspectionRejection> existing = entity.getFinalRejections();

        Map<Long, FinalInspectionRejection> map = existing.stream()
                .filter(e -> e.getSleeperId() != null)
                .collect(Collectors.toMap(FinalInspectionRejection::getSleeperId, e -> e, (a, b) -> a));

        existing.clear();

        if (dtoList != null) {
            for (RejectedDto dto : dtoList) {

                FinalInspectionRejection item = dto.getSleeperId() != null ? map.get(dto.getSleeperId()) : null;

                if (item == null) {
                    item = FinalInspectionRejection.builder()
                            .sleeperId(dto.getSleeperId())
                            .sleeperCode(dto.getSleeperCode())
                            .reason(dto.getReason())
                            .sleeperFinalResultId(dto.getSleeperFinalResultId())
                            .batch(entity)
                            .build();
                } else {
                    item.setReason(dto.getReason());
                    item.setSleeperFinalResultId(dto.getSleeperFinalResultId());
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
                                    .sleeperFinalResultId(s.getSleeperFinalResultId())
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
                                    .reason(s.getReason())
                                    .sleeperFinalResultId(s.getSleeperFinalResultId())
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
                                    .sleeperFinalResultId(s.getSleeperFinalResultId())
                                    .batch(entity)
                                    .build()
                    ));
        }

        // Auto-link SleeperFinalResultId if available
        if (dto.getCallNo() != null) {
            sleeperFinalResultRepository.findByCallNumber(dto.getCallNo().trim()).ifPresent(sfr -> {
                Long sfrId = sfr.getId();
                if (entity.getRejectedSleepers() != null) {
                    entity.getRejectedSleepers().forEach(r -> {
                        if (r.getSleeperFinalResultId() == null) r.setSleeperFinalResultId(sfrId);
                    });
                }
                if (entity.getFinalRejections() != null) {
                    entity.getFinalRejections().forEach(r -> {
                        if (r.getSleeperFinalResultId() == null) r.setSleeperFinalResultId(sfrId);
                    });
                }
                if (entity.getEtSleepers() != null) {
                    entity.getEtSleepers().forEach(et -> {
                        if (et.getSleeperFinalResultId() == null) et.setSleeperFinalResultId(sfrId);
                    });
                }
            });
        }
    }

    private FinalCallResponseDto mapToResponse(IEBatchSummary entity) {

        FinalCallResponseDto dto = new FinalCallResponseDto();

        dto.setId(entity.getId());
        dto.setBatchNo(entity.getBatchNo());
        dto.setCallNo(entity.getCallNo());
        dto.setDateCasted(entity.getDateCasted());
        dto.setCasted(entity.getCasted());
        dto.setOfferedPrev(entity.getOfferedPrev());
        dto.setOfferedNow(entity.getOfferedNow());
        dto.setPassed(entity.getPassed());
        dto.setRejected(entity.getRejected());
        dto.setTotalOffered(entity.getTotalOffered());
        dto.setTotalAccepted(entity.getTotalAccepted());
        dto.setTotalRejected(entity.getTotalRejected());
        dto.setShift(entity.getShift());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());

        dto.setGoodSleepers(mapGood(entity.getGoodSleepers()));
        dto.setRejectedSleepers(mapRejected(entity.getRejectedSleepers()));
        dto.setEtSleepers(mapGood(entity.getEtSleepers()));
        dto.setMfSleepers(mapGood(entity.getMfSleepers()));
        dto.setFinalRejections(mapRejected(entity.getFinalRejections()));

        return dto;
    }

    private List<SleeperDto> mapGood(List<? extends Object> list) {
        return list.stream().map(obj -> {
            SleeperDto dto = new SleeperDto();
            if (obj instanceof FinalGoodSleepers s) {
                dto.setSleeperId(s.getSleeperId());
                dto.setSleeperCode(s.getSleeperCode());
            } else if (obj instanceof FinalCallETSleeper s) {
                dto.setSleeperId(s.getSleeperId());
                dto.setSleeperCode(s.getSleeperCode());
                dto.setReason(s.getReason());
                dto.setSleeperFinalResultId(s.getSleeperFinalResultId());
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
                dto.setSleeperFinalResultId(s.getSleeperFinalResultId());
            } else if (obj instanceof FinalInspectionRejection s) {
                dto.setSleeperId(s.getSleeperId());
                dto.setSleeperCode(s.getSleeperCode());
                dto.setReason(s.getReason());
                dto.setSleeperFinalResultId(s.getSleeperFinalResultId());
            }
            return dto;
        }).toList();
    }

    public FinalCallInspectionHeaderResponse create(FinalCallInspectionHeaderRequest dto) {

        FinalCallInspectionHeader entity = new FinalCallInspectionHeader();

        mapFields(entity, dto);

        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());

        FinalCallInspectionHeader saved = finalCallInspectionHeaderRepository.save(entity);

        return mapToResponse(saved);
    }

    // ================= UPDATE (BY CALL NO) =================
    public FinalCallInspectionHeaderResponse update(FinalCallInspectionHeaderRequest dto) {

        FinalCallInspectionHeader entity = finalCallInspectionHeaderRepository.findByCallNo(dto.getCallNo())
                .orElseThrow(() -> new RuntimeException(
                        "Data not found for callNo: " + dto.getCallNo()));

        mapFields(entity, dto);

        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        finalCallInspectionHeaderRepository.save(entity);

        return mapToResponse(entity);
    }

    // ================= GET BY CALL NO =================
    public FinalCallInspectionHeaderResponse getHeaderByCallNo(String callNo) {

        FinalCallInspectionHeader entity = finalCallInspectionHeaderRepository.findByCallNo(callNo)
                .orElseThrow(() -> new RuntimeException(
                        "Data not found for callNo: " + callNo));

        return mapToResponse(entity);
    }

    private void mapFields(FinalCallInspectionHeader entity,
                           FinalCallInspectionHeaderRequest dto) {

        entity.setCallNo(dto.getCallNo());

        entity.setRlyPoNo(dto.getRlyPoNo());

        if (dto.getPoDate() != null) {
            entity.setPoDate(CommonUtils.convertStringToDateObject(dto.getPoDate()));
        }

        entity.setVendorName(dto.getVendorName());
        entity.setPoQty(dto.getPoQty());

        entity.setMaNo(dto.getMaNo());

        if (dto.getMaDate() != null) {
            entity.setMaDate(CommonUtils.convertStringToDateObject(dto.getMaDate()));
        }

        entity.setQtyOfferedNow(dto.getQtyOfferedNow());
        entity.setAcceptedQty(dto.getAcceptedQty());
        entity.setRejectedQty(dto.getRejectedQty());

        entity.setEtSleepers(dto.getEtSleepers());

        if (dto.getCallDate() != null) {
            entity.setCallDate(CommonUtils.convertStringToDateObject(dto.getCallDate()));
        }

        entity.setNoOfBatches(dto.getNoOfBatches());

        entity.setShift(dto.getShift());
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
    }

    private FinalCallInspectionHeaderResponse mapToResponse(FinalCallInspectionHeader e) {

        return FinalCallInspectionHeaderResponse.builder()
                .id(e.getId())
                .callNo(e.getCallNo())

                .rlyPoNo(e.getRlyPoNo())
                .poDate(e.getPoDate())
                .vendorName(e.getVendorName())

                .poQty(e.getPoQty())
                .maNo(e.getMaNo())
                .maDate(e.getMaDate())

                .qtyOfferedNow(e.getQtyOfferedNow())
                .acceptedQty(e.getAcceptedQty())
                .rejectedQty(e.getRejectedQty())

                .etSleepers(e.getEtSleepers())
                .callDate(e.getCallDate())
                .noOfBatches(e.getNoOfBatches())

                .shift(e.getShift())
                .plantId(e.getPlantId())
                .vendorCode(e.getVendorCode())

                .build();
    }

    @Transactional
    @Override
    public SleeperFinalResult saveOrUpdateSleeperFinalResult(SleeperFinalResultRequestDto dto) {
        if (dto.getCallNumber() == null || dto.getCallNumber().trim().isEmpty()) {
            throw new RuntimeException("Call number cannot be empty");
        }

        SleeperFinalResult result = sleeperFinalResultRepository.findByCallNumber(dto.getCallNumber().trim())
                .orElseGet(() -> {
                    SleeperFinalResult newResult = new SleeperFinalResult();
                    newResult.setCreatedAt(java.time.LocalDateTime.now());
                    newResult.setCreatedBy(dto.getCreatedBy());
                    return newResult;
                });

        result.setCallNumber(dto.getCallNumber().trim());
        result.setPoNo(dto.getPoNo());
        result.setSrNo(dto.getSrNo());
        result.setShift(dto.getShift());
        if (dto.getDateOfInspection() != null && !dto.getDateOfInspection().trim().isEmpty()) {
            try {
                result.setDateOfInspection(CommonUtils.convertStringToDateObject(dto.getDateOfInspection()));
            } catch (Exception e) {
                try {
                    result.setDateOfInspection(LocalDate.parse(dto.getDateOfInspection()));
                } catch (Exception ex) {
                    result.setDateOfInspection(LocalDate.now());
                }
            }
        } else if (result.getDateOfInspection() == null) {
            result.setDateOfInspection(LocalDate.now());
        }

        result.setSleeperType(dto.getSleeperType());
        result.setTotalOfferedQuantity(dto.getTotalOfferedQuantity());
        result.setTotalAccepted(dto.getTotalAccepted());
        result.setTotalRejected(dto.getTotalRejected());
        result.setPlantId(dto.getPlantId());
        result.setUpdatedBy(dto.getUpdatedBy());
        result.setUpdatedAt(java.time.LocalDateTime.now());

        SleeperFinalResult saved = sleeperFinalResultRepository.save(result);

        // Save / Update batch results
        if (dto.getBatches() != null && !dto.getBatches().isEmpty()) {
            // Remove existing batch results if any
            sleeperBatchResultRepository.deleteBySleeperFinalResultId(saved.getId());
            sleeperBatchResultRepository.flush();

            List<SleeperBatchResult> batchEntities = new java.util.ArrayList<>();
            ObjectMapper objectMapper = new ObjectMapper();

            for (SleeperBatchResultDto bDto : dto.getBatches()) {
                String rejectedJson = "[]";
                String etJson = "[]";
                try {
                    if (bDto.getRejectedSleepers() != null) {
                        rejectedJson = objectMapper.writeValueAsString(bDto.getRejectedSleepers());
                    }
                    if (bDto.getEpoxyTreatedSleepers() != null) {
                        etJson = objectMapper.writeValueAsString(bDto.getEpoxyTreatedSleepers());
                    }
                } catch (Exception e) {
                    System.err.println("Error serializing sleepers JSON: " + e.getMessage());
                }

                SleeperBatchResult bEntity = SleeperBatchResult.builder()
                        .sleeperFinalResult(saved)
                        .batchNo(bDto.getBatchNo())
                        .batchOfferedQuantity(bDto.getBatchOfferedQuantity())
                        .batchPassedQuantity(bDto.getBatchPassedQuantity())
                        .batchRejectedQuantity(bDto.getBatchRejectedQuantity())
                        .rejectedSleepers(rejectedJson)
                        .epoxyTreatedSleepers(etJson)
                        .createdAt(java.time.LocalDateTime.now())
                        .updatedAt(java.time.LocalDateTime.now())
                        .build();

                batchEntities.add(bEntity);
            }
            sleeperBatchResultRepository.saveAll(batchEntities);
        }

        return saved;
    }

    @Override
    public SleeperFinalResult getSleeperFinalResult(String callNumber) {
        return sleeperFinalResultRepository.findByCallNumber(callNumber).orElse(null);
    }
}
