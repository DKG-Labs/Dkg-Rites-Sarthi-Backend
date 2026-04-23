package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.SleeperInspectionCallSubmitDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.SleeperInspectionCallBatchDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.SleeperInspectionCallListDto;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCall;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCallBatch;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperDetail;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.SleeperInspectionCallRepository;
import com.sarthi.Sleeper.service.SleeperInspectionCallService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SleeperInspectionCallServiceImpl implements SleeperInspectionCallService {

    private final SleeperInspectionCallRepository inspectionCallRepository;

    @Override
    @Transactional
    public String submitInspectionCall(SleeperInspectionCallSubmitDto dto) {
        SleeperInspectionCall call = new SleeperInspectionCall();

        // Generate callNo SF-DDMM0001
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);
        long countToday = inspectionCallRepository.countByCreatedAtBetween(startOfDay, endOfDay);
        String datePart = String.format("%02d%02d", startOfDay.getDayOfMonth(), startOfDay.getMonthValue());
        String sequencePart = String.format("%04d", countToday + 1);
        call.setCallNo("SF-" + datePart + sequencePart);

        call.setPoNo(dto.getPoNo());
        call.setSrNo(dto.getSrNo());
        call.setSleeperType(dto.getSleeperType());
        call.setTotalOffered(dto.getTotalOffered());
        call.setTotalRejected(dto.getTotalRejected());
        call.setCreatedBy(dto.getCreatedBy());
        call.setCreatedAt(LocalDateTime.now());
        call.setStatus("Pending for verification");

        List<SleeperInspectionCallBatch> batchEntities = new ArrayList<>();

        if (dto.getBatchesSelected() != null) {
            for (SleeperInspectionCallBatchDto batchDto : dto.getBatchesSelected()) {
                SleeperInspectionCallBatch batchEntity = new SleeperInspectionCallBatch();
                batchEntity.setInspectionCall(call);
                batchEntity.setBatchNo(batchDto.getBatchNo());
                
                List<SleeperDetail> goodDetails = new ArrayList<>();
                if (batchDto.getGoodSleepers() != null && batchDto.getGoodSleeperIds() != null) {
                    for (int i = 0; i < batchDto.getGoodSleepers().size(); i++) {
                        String sno = batchDto.getGoodSleepers().get(i);
                        Long sid = (i < batchDto.getGoodSleeperIds().size()) ? batchDto.getGoodSleeperIds().get(i) : null;
                        goodDetails.add(new SleeperDetail(sno, sid));
                    }
                }
                batchEntity.setGoodSleepers(goodDetails);

                List<SleeperDetail> badDetails = new ArrayList<>();
                if (batchDto.getBadSleepers() != null && batchDto.getBadSleeperIds() != null) {
                    for (int i = 0; i < batchDto.getBadSleepers().size(); i++) {
                        String sno = batchDto.getBadSleepers().get(i);
                        Long sid = (i < batchDto.getBadSleeperIds().size()) ? batchDto.getBadSleeperIds().get(i) : null;
                        badDetails.add(new SleeperDetail(sno, sid));
                    }
                }
                batchEntity.setBadSleepers(badDetails);
                
                batchEntities.add(batchEntity);
            }
        }
        
        call.setBatchesSelected(batchEntities);
        inspectionCallRepository.save(call);
        return call.getCallNo();
    }

    @Override
    public List<SleeperInspectionCallListDto> getVendorInspectionCalls(Long userId) {
        List<SleeperInspectionCall> calls = inspectionCallRepository.findByCreatedBy(userId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return calls.stream().map(call -> {
            SleeperInspectionCallListDto dto = new SleeperInspectionCallListDto();
            dto.setId(call.getId());
            dto.setCallNo(call.getCallNo());
            dto.setPoNo(call.getPoNo());
            dto.setSrNo(call.getSrNo());
            dto.setCallDate(call.getCreatedAt() != null ? call.getCreatedAt().format(formatter) : "N/A");
            dto.setSleeperType(call.getSleeperType());
            dto.setQtyOffered(call.getTotalOffered());
            dto.setBatches(call.getBatchesSelected() != null ? call.getBatchesSelected().size() : 0);
            dto.setStatus(call.getStatus());
            return dto;
        }).collect(Collectors.toList());
    }
}
