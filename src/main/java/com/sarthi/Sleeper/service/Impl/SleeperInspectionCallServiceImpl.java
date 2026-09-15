package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.SleeperInspectionCallDetailDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.SleeperInspectionCallSubmitDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.SleeperInspectionCallBatchDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.SleeperInspectionCallListDto;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCall;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCallBatch;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperDetail;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.SleeperInspectionCallRepository;
import com.sarthi.Sleeper.repository.SleeperWorkflowRepository;
import com.sarthi.Sleeper.service.SleeperInspectionCallService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SleeperInspectionCallServiceImpl implements SleeperInspectionCallService {

    private final SleeperInspectionCallRepository inspectionCallRepository;
    private final SleeperWorkflowRepository sleeperWorkflowRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public String submitInspectionCall(SleeperInspectionCallSubmitDto dto) {
        SleeperInspectionCall call = new SleeperInspectionCall();

        // Generate unique callNo SF-MMddyy001 in IST (Indian Standard Time)
        LocalDateTime now = LocalDateTime.now(java.time.ZoneId.of("Asia/Kolkata"));
        String datePart = now.format(DateTimeFormatter.ofPattern("MMddyy"));
        String prefix = "SF-" + datePart;
        
        long seq = 1;
        while (inspectionCallRepository.existsByCallNo(String.format("%s%03d", prefix, seq))) {
            seq++;
        }
        call.setCallNo(String.format("%s%03d", prefix, seq));

        call.setPoNo(dto.getPoNo());
        call.setSrNo(dto.getSrNo());
        call.setSleeperType(dto.getSleeperType());
        call.setTotalOffered(dto.getTotalOffered());
        call.setTotalRejected(dto.getTotalRejected());
        call.setDesiredInspectionDate(dto.getDesiredInspectionDate() != null ? dto.getDesiredInspectionDate() : java.time.LocalDate.now());
        call.setCreatedBy(dto.getCreatedBy());
        call.setPlantId(dto.getPlantId());
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
                batchEntity.setTotalCasted(batchDto.getTotalCasted());
                batchEntity.setCastDate(batchDto.getCastDate());
                batchEntity.setPreviouslyOffered(batchDto.getPreviouslyOffered());
                
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
            dto.setDesiredInspectionDate(call.getDesiredInspectionDate());
            dto.setSleeperType(call.getSleeperType());
            int off = call.getTotalOffered() != null ? call.getTotalOffered() : 0;
            int rej = call.getTotalRejected() != null ? call.getTotalRejected() : 0;
            dto.setQtyOffered(off + rej);
            dto.setBatches(call.getBatchesSelected() != null ? call.getBatchesSelected().size() : 0);
            dto.setStatus(call.getStatus());
            dto.setPlantId(call.getPlantId());

            String uom = null;
            try {
                List<String> uomList = jdbcTemplate.query(
                    "SELECT pi.uom FROM po_item pi JOIN po_header ph ON pi.po_header_id = ph.id WHERE ph.po_no = ? LIMIT 1",
                    (rs, rowNum) -> rs.getString("uom"),
                    call.getPoNo()
                );
                if (uomList != null && !uomList.isEmpty() && uomList.get(0) != null && !uomList.get(0).isBlank()) {
                    uom = uomList.get(0).trim();
                }
            } catch (Exception ignored) {}

            if (uom == null || uom.isBlank()) {
                String st = call.getSleeperType() != null ? call.getSleeperType().toUpperCase() : "";
                if (st.contains("SET") || st.contains("PNC") || st.contains("TURNOUT") || st.contains("8746") || st.contains("4218") || st.contains("4865") || st.contains("9790") || st.contains("4732") || st.contains("DERAIL")) {
                    uom = "Set";
                } else {
                    uom = "Nos.";
                }
            }
            dto.setUom(uom);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String withdrawInspectionCall(String callNo) {
        if (callNo == null || callNo.isBlank()) {
            throw new IllegalArgumentException("Call Number is required");
        }
        String trimmedCallNo = callNo.trim();
        Optional<SleeperInspectionCall> callOpt = inspectionCallRepository.findByCallNoWithBatches(trimmedCallNo);
        if (callOpt.isEmpty()) {
            callOpt = inspectionCallRepository.findByCallNo(trimmedCallNo);
        }

        if (callOpt.isPresent()) {
            SleeperInspectionCall call = callOpt.get();
            inspectionCallRepository.delete(call);
        }

        // Delete workflow transaction records for this call
        sleeperWorkflowRepository.deleteByRequestId(trimmedCallNo);

        return trimmedCallNo;
    }

    @Override
    @Transactional(readOnly = true)
    public SleeperInspectionCallDetailDto getInspectionCallDetails(String callNo) {
        if (callNo == null || callNo.isBlank()) {
            throw new IllegalArgumentException("Call Number is required");
        }
        String trimmedCallNo = callNo.trim();
        SleeperInspectionCall call = inspectionCallRepository.findByCallNoWithBatches(trimmedCallNo)
                .orElseGet(() -> inspectionCallRepository.findByCallNo(trimmedCallNo)
                        .orElseThrow(() -> new RuntimeException("Inspection Call not found: " + trimmedCallNo)));

        SleeperInspectionCallDetailDto dto = new SleeperInspectionCallDetailDto();
        dto.setId(call.getId());
        dto.setCallNo(call.getCallNo());
        dto.setPoNo(call.getPoNo());
        dto.setSrNo(call.getSrNo());
        dto.setSleeperType(call.getSleeperType());
        dto.setTotalOffered(call.getTotalOffered());
        dto.setTotalRejected(call.getTotalRejected());
        dto.setDesiredInspectionDate(call.getDesiredInspectionDate());
        dto.setStatus(call.getStatus());
        dto.setCreatedBy(call.getCreatedBy());
        dto.setPlantId(call.getPlantId());
        dto.setCreatedAt(call.getCreatedAt());

        String detailUom = null;
        try {
            List<String> uomList = jdbcTemplate.query(
                "SELECT pi.uom FROM po_item pi JOIN po_header ph ON pi.po_header_id = ph.id WHERE ph.po_no = ? LIMIT 1",
                (rs, rowNum) -> rs.getString("uom"),
                call.getPoNo()
            );
            if (uomList != null && !uomList.isEmpty() && uomList.get(0) != null && !uomList.get(0).isBlank()) {
                detailUom = uomList.get(0).trim();
            }
        } catch (Exception ignored) {}

        if (detailUom == null || detailUom.isBlank()) {
            String st = call.getSleeperType() != null ? call.getSleeperType().toUpperCase() : "";
            if (st.contains("SET") || st.contains("PNC") || st.contains("TURNOUT") || st.contains("8746") || st.contains("4218") || st.contains("4865") || st.contains("9790") || st.contains("4732") || st.contains("DERAIL")) {
                detailUom = "Set";
            } else {
                detailUom = "Nos.";
            }
        }
        dto.setUom(detailUom);

        List<SleeperInspectionCallBatchDto> batchDtos = new ArrayList<>();
        if (call.getBatchesSelected() != null) {
            for (SleeperInspectionCallBatch b : call.getBatchesSelected()) {
                SleeperInspectionCallBatchDto bDto = new SleeperInspectionCallBatchDto();
                bDto.setBatchNo(b.getBatchNo());

                List<String> goodSleepers = new ArrayList<>();
                List<Long> goodSleeperIds = new ArrayList<>();
                if (b.getGoodSleepers() != null) {
                    for (SleeperDetail sd : b.getGoodSleepers()) {
                        if (sd != null) {
                            if (sd.getSleeperNo() != null) goodSleepers.add(sd.getSleeperNo());
                            if (sd.getSleeperId() != null) goodSleeperIds.add(sd.getSleeperId());
                        }
                    }
                }
                bDto.setGoodSleepers(goodSleepers);
                bDto.setGoodSleeperIds(goodSleeperIds);

                List<String> badSleepers = new ArrayList<>();
                List<Long> badSleeperIds = new ArrayList<>();
                if (b.getBadSleepers() != null) {
                    for (SleeperDetail sd : b.getBadSleepers()) {
                        if (sd != null) {
                            if (sd.getSleeperNo() != null) badSleepers.add(sd.getSleeperNo());
                            if (sd.getSleeperId() != null) badSleeperIds.add(sd.getSleeperId());
                        }
                    }
                }
                bDto.setBadSleepers(badSleepers);
                bDto.setBadSleeperIds(badSleeperIds);

                bDto.setTotalCasted(b.getTotalCasted());
                bDto.setCastDate(b.getCastDate());
                bDto.setPreviouslyOffered(b.getPreviouslyOffered());

                batchDtos.add(bDto);
            }
        }
        dto.setBatchesSelected(batchDtos);
        return dto;
    }

    @Override
    @Transactional
    public String modifyInspectionCall(SleeperInspectionCallSubmitDto dto) {
        if (dto.getCallNo() == null || dto.getCallNo().isBlank()) {
            throw new IllegalArgumentException("Call Number is required for modification");
        }
        String trimmedCallNo = dto.getCallNo().trim();
        SleeperInspectionCall call = inspectionCallRepository.findByCallNoWithBatches(trimmedCallNo)
                .orElseGet(() -> inspectionCallRepository.findByCallNo(trimmedCallNo)
                        .orElseThrow(() -> new RuntimeException("Inspection Call not found: " + trimmedCallNo)));

        if (dto.getPoNo() != null) call.setPoNo(dto.getPoNo());
        if (dto.getSrNo() != null) call.setSrNo(dto.getSrNo());
        if (dto.getSleeperType() != null) call.setSleeperType(dto.getSleeperType());
        if (dto.getTotalOffered() != null) call.setTotalOffered(dto.getTotalOffered());
        if (dto.getTotalRejected() != null) call.setTotalRejected(dto.getTotalRejected());
        if (dto.getDesiredInspectionDate() != null) call.setDesiredInspectionDate(dto.getDesiredInspectionDate());
        if (dto.getPlantId() != null) call.setPlantId(dto.getPlantId());

        // Clear existing batches and repopulate cleanly via JPA cascade and orphanRemoval
        if (call.getBatchesSelected() != null) {
            call.getBatchesSelected().clear();
        } else {
            call.setBatchesSelected(new ArrayList<>());
        }

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
                batchEntity.setTotalCasted(batchDto.getTotalCasted());
                batchEntity.setCastDate(batchDto.getCastDate());
                batchEntity.setPreviouslyOffered(batchDto.getPreviouslyOffered());

                call.getBatchesSelected().add(batchEntity);
            }
        }

        inspectionCallRepository.save(call);
        return call.getCallNo();
    }
}
