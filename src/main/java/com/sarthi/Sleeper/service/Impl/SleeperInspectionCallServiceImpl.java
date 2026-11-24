package com.sarthi.Sleeper.service.Impl;

import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.SleeperInspectionCallDetailDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.SleeperInspectionCallSubmitDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.SleeperInspectionCallBatchDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.SleeperInspectionCallListDto;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCall;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCallBatch;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperDetail;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.SleeperInspectionCallRepository;
import com.sarthi.Sleeper.repository.SleeperWorkflowRepository;
import com.sarthi.Sleeper.entity.SleeperWorkflowTransaction;

import com.sarthi.Sleeper.service.SleeperInspectionCallService;
import com.sarthi.Sleeper.service.SleeperWorkflowService;
import com.sarthi.Sleeper.util.SleeperQtyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SleeperInspectionCallServiceImpl implements SleeperInspectionCallService {

    private final SleeperInspectionCallRepository inspectionCallRepository;
    private final SleeperWorkflowRepository sleeperWorkflowRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    private final SleeperWorkflowService sleeperWorkflowService;
    private final com.sarthi.repository.SarthiIbsPoiMappingRepository ibsPoiMappingRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.sarthi.Sleeper.service.WaterCubeLotService waterCubeLotService;

    @org.springframework.beans.factory.annotation.Autowired
    public SleeperInspectionCallServiceImpl(
            SleeperInspectionCallRepository inspectionCallRepository,
            SleeperWorkflowRepository sleeperWorkflowRepository,
            org.springframework.jdbc.core.JdbcTemplate jdbcTemplate,
            SleeperWorkflowService sleeperWorkflowService,
            com.sarthi.repository.SarthiIbsPoiMappingRepository ibsPoiMappingRepository) {
        this.inspectionCallRepository = inspectionCallRepository;
        this.sleeperWorkflowRepository = sleeperWorkflowRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.sleeperWorkflowService = sleeperWorkflowService;
        this.ibsPoiMappingRepository = ibsPoiMappingRepository;
    }

    /** Lots rejected by a failed water cube test are returned to the vendor and cannot be offered in a call. */
    private void assertNoRejectedLots(SleeperInspectionCallSubmitDto dto, String plantId) {
        if (dto.getBatchesSelected() == null) {
            return;
        }
        for (SleeperInspectionCallBatchDto batchDto : dto.getBatchesSelected()) {
            if (batchDto != null) {
                waterCubeLotService.assertNotRejected(batchDto.getBatchNo(), plantId);
            }
        }
    }

    @Override
    @Transactional
    public String submitInspectionCall(SleeperInspectionCallSubmitDto dto) {
        // Block plant/vendor if chargeable cancellation payment is pending
        if (dto.getPlantId() != null && !dto.getPlantId().isBlank()
                && sleeperWorkflowService.isPlantBlockedForCallRaising(dto.getPlantId(), dto.getVendorCode())) {
            throw new BusinessException(new ErrorDetails(
                    AppConstant.INTER_SERVER_ERROR,
                    AppConstant.ERROR_TYPE_CODE_INTERNAL,
                    AppConstant.ERROR_TYPE_ERROR,
                    "Call raising is blocked for plant (" + dto.getPlantId()
                    + ") due to pending cancellation charges. Please clear outstanding payment in Payment Details module."));
        }

        // Block if IBS Vendor Code is not configured for this plant
        if (dto.getPlantId() != null && !dto.getPlantId().isBlank()) {
            java.util.Optional<com.sarthi.entity.IBS.SarthiIbsPoiMapping> ibsMapping =
                    ibsPoiMappingRepository.findBestMatch(dto.getPlantId(), "sleeper");
            if (ibsMapping.isEmpty() || ibsMapping.get().getIbsVendorCode() == null
                    || ibsMapping.get().getIbsVendorCode().isBlank()) {
                throw new BusinessException(new ErrorDetails(
                        AppConstant.INTER_SERVER_ERROR,
                        AppConstant.ERROR_TYPE_CODE_INTERNAL,
                        AppConstant.ERROR_TYPE_ERROR,
                        "Call raising is blocked: IBS Vendor Code is not configured for Plant ("
                        + dto.getPlantId() + "). Please contact RITES admin to update the IBS mapping."));
            }
        }

        assertNoRejectedLots(dto, dto.getPlantId());

        SleeperInspectionCall call = new SleeperInspectionCall();

        // Generate unique callNo SF-yyMMddNN in IST (Indian Standard Time)
        LocalDateTime now = LocalDateTime.now(java.time.ZoneId.of("Asia/Kolkata"));
        String datePart = now.format(DateTimeFormatter.ofPattern("yyMMdd"));
        String prefix = "SF-" + datePart;

        long seq = 1;
        while (inspectionCallRepository.existsByCallNo(String.format("%s%02d", prefix, seq))) {
            seq++;
        }
        call.setCallNo(String.format("%s%02d", prefix, seq));

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

            // Quantities are decimal: RMT calls can carry fractional offered / accepted / rejected qty.
            java.math.BigDecimal finalOffered = SleeperQtyUtil.toQty(call.getTotalOffered());
            java.math.BigDecimal finalAccepted = null;
            java.math.BigDecimal finalRejected = SleeperQtyUtil.toQty(call.getTotalRejected());
            // call.totalOffered is already the PASSED count (vendor form saves good sleepers only; rejections are in
            // totalRejected). Only an IE header's qty_offered_now is a gross offered qty that still includes rejections.
            boolean offeredIncludesRejected = false;

            try {
                List<java.util.Map<String, Object>> fchList = jdbcTemplate.queryForList(
                    "SELECT qty_offered_now, accepted_qty, rejected_qty FROM final_call_inspection_header WHERE call_no = ? ORDER BY id DESC LIMIT 1",
                    call.getCallNo()
                );
                if (fchList != null && !fchList.isEmpty()) {
                    java.util.Map<String, Object> fch = fchList.get(0);
                    Object qOff = fch.get("qty_offered_now");
                    Object qAcc = fch.get("accepted_qty");
                    Object qRej = fch.get("rejected_qty");
                    if (SleeperQtyUtil.isPositive(SleeperQtyUtil.toQty(qOff))) {
                        finalOffered = SleeperQtyUtil.toQty(qOff);
                        offeredIncludesRejected = true;
                    }
                    if (qAcc != null) {
                        finalAccepted = SleeperQtyUtil.toQty(qAcc);
                    }
                    if (qRej != null) {
                        finalRejected = SleeperQtyUtil.toQty(qRej);
                    }
                }
            } catch (Exception ignored) {}

            String sType = call.getSleeperType() != null ? call.getSleeperType().toUpperCase() : "";
            boolean isTurnoutOrSet = sType.contains("TURNOUT") 
                    || sType.contains("SET") 
                    || sType.contains("RT-8838") 
                    || sType.contains("RT-6068")
                    || sType.contains("RT-4865")
                    || sType.contains("RT-9790")
                    || sType.contains("RT-4218")
                    || sType.contains("POINTS")
                    || sType.contains("CROSSING");

            // Set/Turnout: the call's total_offered is the vendor's offered SETS; always show it.
            if (isTurnoutOrSet && call.getTotalOffered() != null && call.getTotalOffered().signum() > 0) {
                finalOffered = SleeperQtyUtil.toQty(call.getTotalOffered());
            }

            if (finalAccepted == null && finalOffered != null) {
                finalAccepted = offeredIncludesRejected
                        ? SleeperQtyUtil.acceptedOf(finalOffered, finalRejected)
                        : finalOffered;
            }

            dto.setQtyOffered(SleeperQtyUtil.orZero(finalOffered));
            dto.setAcceptedQty(SleeperQtyUtil.orZero(finalAccepted));
            dto.setRejectedQty(SleeperQtyUtil.orZero(finalRejected));
            dto.setBatches(call.getBatchesSelected() != null ? call.getBatchesSelected().size() : 0);
            String effectiveStatus = call.getStatus();
            String jobStatus = null;
            LocalDate scheduledDate = null;
            String ieName = "Not Assigned";
            String ieContactNo = "";
            try {
                List<java.util.Map<String, Object>> wfList = jdbcTemplate.queryForList(
                    "SELECT swt.status, swt.job_status FROM sleeper_workflow_transaction swt WHERE swt.request_id = ? ORDER BY swt.workflow_transition_id DESC LIMIT 1",
                    call.getCallNo()
                );
                if (wfList != null && !wfList.isEmpty()) {
                    java.util.Map<String, Object> latestRow = wfList.get(0);
                    Object s = latestRow.get("status");
                    Object js = latestRow.get("job_status");
                    if (s != null && !s.toString().isBlank()) {
                        effectiveStatus = s.toString().trim();
                    }
                    if (js != null && !js.toString().isBlank()) {
                        jobStatus = js.toString().trim();
                    }
                }

                // Look for assigned IE in transactions for this call
                List<java.util.Map<String, Object>> ieRows = jdbcTemplate.queryForList(
                    "SELECT COALESCE(um.FULL_NAME, um.USERNAME) as ie_name, " +
                    "COALESCE(NULLIF(TRIM(um.MOBILENUMBER), ''), um.ALTERNATE_MOBILE_NUMBER) as ie_mobile " +
                    "FROM sleeper_workflow_transaction swt " +
                    "JOIN user_master um ON um.userid = swt.assigned_to_user " +
                    "WHERE swt.request_id = ? AND swt.assigned_to_user IS NOT NULL " +
                    "ORDER BY swt.workflow_transition_id DESC LIMIT 1",
                    call.getCallNo()
                );
                if (ieRows != null && !ieRows.isEmpty()) {
                    Object ieN = ieRows.get(0).get("ie_name");
                    Object ieM = ieRows.get(0).get("ie_mobile");
                    if (ieN != null && !ieN.toString().isBlank()) ieName = ieN.toString().trim();
                    if (ieM != null && !ieM.toString().isBlank()) ieContactNo = ieM.toString().trim();
                }
            } catch (Exception ignored) {}

            dto.setIeName(ieName);
            dto.setIeContactNo(ieContactNo);
            dto.setIeMobile(ieContactNo);

            try {
                List<java.util.Map<String, Object>> schedList = jdbcTemplate.queryForList(
                    "SELECT schedule_date FROM sleeper_schedule WHERE call_no = ? ORDER BY id DESC LIMIT 1",
                    call.getCallNo()
                );
                if (schedList != null && !schedList.isEmpty()) {
                    Object sd = schedList.get(0).get("schedule_date");
                    if (sd instanceof java.sql.Date) {
                        scheduledDate = ((java.sql.Date) sd).toLocalDate();
                    } else if (sd instanceof java.time.LocalDate) {
                        scheduledDate = (java.time.LocalDate) sd;
                    }
                }
            } catch (Exception ignored) {}

            if (scheduledDate != null) {
                dto.setScheduledDate(scheduledDate);
                if (jobStatus == null || !jobStatus.toUpperCase().contains("INSPECT")) {
                    jobStatus = "SCHEDULED";
                }
            }

            try {
                // 1. Primary Source: sleeper_final_ic_edit (fetch ic_number and created_at)
                if (call.getCallNo() != null) {
                    List<java.util.Map<String, Object>> sfRows = jdbcTemplate.queryForList(
                        "SELECT ic_number, created_at FROM sleeper_final_ic_edit WHERE ic_number = ? OR ic_number LIKE ? OR ic_number LIKE ? ORDER BY id DESC LIMIT 1",
                        call.getCallNo(), "%/" + call.getCallNo() + "/%", "%" + call.getCallNo() + "%"
                    );
                    if (sfRows != null && !sfRows.isEmpty()) {
                        Object certNo = sfRows.get(0).get("ic_number");
                        Object crAt = sfRows.get(0).get("created_at");
                        if (certNo != null && !certNo.toString().isBlank()) dto.setIcNo(certNo.toString().trim());
                        if (crAt instanceof java.time.LocalDateTime) {
                            dto.setIcDate(((java.time.LocalDateTime) crAt).toLocalDate().toString());
                        } else if (crAt instanceof java.sql.Timestamp) {
                            dto.setIcDate(((java.sql.Timestamp) crAt).toLocalDateTime().toLocalDate().toString());
                        } else if (crAt instanceof java.sql.Date) {
                            dto.setIcDate(((java.sql.Date) crAt).toLocalDate().toString());
                        } else if (crAt != null) {
                            String s = crAt.toString().trim();
                            if (s.length() >= 10) dto.setIcDate(s.substring(0, 10));
                        }
                    }
                }

                // 2. Secondary fallback: sleeper_inspection_complete_details (only if missing)
                if ((dto.getIcNo() == null || dto.getIcNo().isBlank() || dto.getIcDate() == null || dto.getIcDate().isBlank()) && call.getCallNo() != null) {
                    List<java.util.Map<String, Object>> icList = jdbcTemplate.queryForList(
                        "SELECT certificate_no, created_on FROM sleeper_inspection_complete_details WHERE call_no = ? ORDER BY id DESC LIMIT 1",
                        call.getCallNo()
                    );
                    if (icList != null && !icList.isEmpty()) {
                        Object certNo = icList.get(0).get("certificate_no");
                        Object crOn = icList.get(0).get("created_on");
                        if ((dto.getIcNo() == null || dto.getIcNo().isBlank()) && certNo != null && !certNo.toString().isBlank()) dto.setIcNo(certNo.toString().trim());
                        if (dto.getIcDate() == null || dto.getIcDate().isBlank()) {
                            if (crOn instanceof java.time.LocalDateTime) {
                                dto.setIcDate(((java.time.LocalDateTime) crOn).toLocalDate().toString());
                            } else if (crOn instanceof java.sql.Timestamp) {
                                dto.setIcDate(((java.sql.Timestamp) crOn).toLocalDateTime().toLocalDate().toString());
                            } else if (crOn != null) {
                                dto.setIcDate(crOn.toString().substring(0, 10));
                            }
                        }
                    }
                }

                // 3. Tertiary fallback: certificate_storage
                if ((dto.getIcNo() == null || dto.getIcNo().isBlank() || dto.getIcDate() == null || dto.getIcDate().isBlank()) && call.getCallNo() != null) {
                    List<java.util.Map<String, Object>> csRows = jdbcTemplate.queryForList(
                        "SELECT ic_number, ic_date, uploaded_at FROM certificate_storage WHERE ic_number = ? OR ic_number LIKE ? OR file_name LIKE ? ORDER BY id DESC LIMIT 1",
                        call.getCallNo(), "%" + call.getCallNo() + "%", "%" + call.getCallNo() + "%"
                    );
                    if (csRows != null && !csRows.isEmpty()) {
                        Object certNo = csRows.get(0).get("ic_number");
                        Object icd = csRows.get(0).get("ic_date");
                        Object up = csRows.get(0).get("uploaded_at");
                        if ((dto.getIcNo() == null || dto.getIcNo().isBlank()) && certNo != null && !certNo.toString().isBlank()) dto.setIcNo(certNo.toString().trim());
                        if (dto.getIcDate() == null || dto.getIcDate().isBlank()) {
                            if (icd != null) {
                                dto.setIcDate(icd.toString());
                            } else if (up instanceof java.time.LocalDateTime) {
                                dto.setIcDate(((java.time.LocalDateTime) up).toLocalDate().toString());
                            } else if (up instanceof java.sql.Timestamp) {
                                dto.setIcDate(((java.sql.Timestamp) up).toLocalDateTime().toLocalDate().toString());
                            }
                        }
                    }
                }

                String stUpper = (effectiveStatus != null ? effectiveStatus : "").toUpperCase();
                if (stUpper.contains("IBS") || stUpper.contains("COMPLET")) {
                    if (dto.getIcNo() == null || dto.getIcNo().isBlank()) {
                        dto.setIcNo("N/" + call.getCallNo() + "/ARV");
                    }
                    if (dto.getIcDate() == null || dto.getIcDate().isBlank()) {
                        if (call.getDesiredInspectionDate() != null) {
                            dto.setIcDate(call.getDesiredInspectionDate().toString());
                        } else if (call.getCreatedAt() != null) {
                            dto.setIcDate(call.getCreatedAt().toLocalDate().toString());
                        }
                    }
                }
            } catch (Exception ignored) {}

            dto.setStatus(effectiveStatus);
            dto.setJobStatus(jobStatus);
            dto.setPlantId(call.getPlantId());

            dto.setUom(resolveCallUom(call.getPoNo(), call.getSrNo(), call.getSleeperType()));
            return dto;
        }).collect(Collectors.toList());
    }

        @Override
    @Transactional
    public String withdrawInspectionCall(String callNo, String remarks, Long actionBy) {
        if (callNo == null || callNo.isBlank()) {
            throw new IllegalArgumentException("Call Number is required");
        }
        String trimmedCallNo = callNo.trim();

        // 1. Latest workflow row: used for validation AND for the WITHDRAW update
        SleeperWorkflowTransaction latestWf =
                sleeperWorkflowRepository.findFirstByRequestIdOrderByWorkflowTransitionIdDesc(trimmedCallNo);

        if (latestWf != null) {
            String js = latestWf.getJobStatus() != null ? latestWf.getJobStatus().trim().toUpperCase() : "";
            String st = latestWf.getStatus() != null ? latestWf.getStatus().trim().toUpperCase() : "";
            if (js.contains(AppConstant.WITHDRAW_TYPE) || st.contains(AppConstant.WITHDRAW_TYPE)) {
                throw new IllegalStateException("Inspection call is already withdrawn.");
            }
            if (js.contains("SCHEDULE")) {
                throw new IllegalStateException("Inspection call is already SCHEDULED and cannot be withdrawn.");
            }
            if (js.contains("PO_VERIF")) {
                throw new IllegalStateException("Inspection call is under PO_VERIFICATION and cannot be withdrawn.");
            }
            if ("COMPLETED".equals(js)) {
                throw new IllegalStateException("Inspection call is COMPLETED and cannot be withdrawn.");
            }
        }

        // 2. Mark the call WITHDRAWN (do NOT delete).
        //    Batch-availability queries in SleeperInspectionCallRepository already skip
        //    status 'WITHDRAWN', so the batches are released automatically.
        SleeperInspectionCall call = inspectionCallRepository.findByCallNo(trimmedCallNo)
                .orElseThrow(() -> new RuntimeException("Inspection Call not found: " + trimmedCallNo));
        call.setStatus(AppConstant.WITHDRAWN_TYPE);
        inspectionCallRepository.save(call);

        // 3. Append a new WITHDRAW workflow row (the previous rows stay untouched, so the
        //    workflow history - e.g. the RIO Help Desk verification - is preserved).
        //    Context fields are carried over from the latest row so every query that reads the
        //    latest row per request (Completed / Pending / CM views) behaves exactly as before.
        //    No row is created when the call has no workflow yet.
        if (latestWf != null) {
            LocalDateTime now = LocalDateTime.now();
            SleeperWorkflowTransaction withdrawWf = new SleeperWorkflowTransaction();
            withdrawWf.setWorkflowId(latestWf.getWorkflowId());
            withdrawWf.setModuleId(latestWf.getModuleId());
            withdrawWf.setRequestId(latestWf.getRequestId());
            withdrawWf.setPlantId(latestWf.getPlantId());
            withdrawWf.setVendorCode(latestWf.getVendorCode());
            withdrawWf.setPoiCode(latestWf.getPoiCode());
            withdrawWf.setRio(latestWf.getRio());
            withdrawWf.setShift(latestWf.getShift());
            withdrawWf.setAssignedToUser(latestWf.getAssignedToUser());
            withdrawWf.setCreatedBy(latestWf.getCreatedBy());
            withdrawWf.setStatus(AppConstant.WITHDRAW_TYPE);
            withdrawWf.setAction(AppConstant.WITHDRAW_TYPE);
            withdrawWf.setJobStatus(AppConstant.WITHDRAW_TYPE);
            withdrawWf.setCurrentRole(resolveUserRole(actionBy));
            withdrawWf.setNextRole(null);
            withdrawWf.setRemarks(remarks != null && !remarks.isBlank() ? remarks.trim() : null);
            withdrawWf.setModifiedBy(actionBy);
            withdrawWf.setCreatedDate(now);
            withdrawWf.setUpdatedDate(now);
            sleeperWorkflowRepository.save(withdrawWf);
        }

        return trimmedCallNo;
    }

    /** Role name of the user performing an action, read from user_master; null when unknown. */
    private String resolveUserRole(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            List<String> roles = jdbcTemplate.queryForList(
                    "SELECT role_name FROM user_master WHERE userid = ?", String.class, userId);
            return roles.isEmpty() ? null : roles.get(0);
        } catch (Exception e) {
            return null;
        }
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

        dto.setUom(resolveCallUom(call.getPoNo(), call.getSrNo(), call.getSleeperType()));

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

        // Check if call is COMPLETED in sleeper_workflow_transaction
        try {
            List<String> jsList = jdbcTemplate.query(
                "SELECT swt.job_status FROM sleeper_workflow_transaction swt WHERE swt.request_id = ? ORDER BY swt.workflow_transition_id DESC LIMIT 1",
                (rs, rowNum) -> rs.getString("job_status"),
                trimmedCallNo
            );
            if (jsList != null && !jsList.isEmpty() && jsList.get(0) != null) {
                String js = jsList.get(0).trim().toUpperCase();
                if ("COMPLETED".equals(js)) {
                    throw new IllegalStateException("Inspection call is COMPLETED and cannot be modified.");
                }
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception ignored) {}

        SleeperInspectionCall call = inspectionCallRepository.findByCallNoWithBatches(trimmedCallNo)
                .orElseGet(() -> inspectionCallRepository.findByCallNo(trimmedCallNo)
                        .orElseThrow(() -> new RuntimeException("Inspection Call not found: " + trimmedCallNo)));

        assertNoRejectedLots(dto, dto.getPlantId() != null && !dto.getPlantId().isBlank() ? dto.getPlantId() : call.getPlantId());

        if (dto.getPoNo() != null) call.setPoNo(dto.getPoNo());
        if (dto.getSrNo() != null) call.setSrNo(dto.getSrNo());
        if (dto.getSleeperType() != null) call.setSleeperType(dto.getSleeperType());
        if (dto.getTotalOffered() != null) {
            BigDecimal previousQty = call.getTotalOffered();
            if (previousQty != null && previousQty.compareTo(BigDecimal.ZERO) > 0 && dto.getTotalOffered().compareTo(previousQty) < 0) {
                throw new IllegalArgumentException("Modification not allowed: Call quantity cannot be decreased. Previous quantity: " 
                        + previousQty + ", entered: " + dto.getTotalOffered());
            }
            call.setTotalOffered(dto.getTotalOffered());
        }
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

    private String resolveCallUom(String poNo, String srNo, String sleeperType) {
        String uom = null;
        if (poNo != null && !poNo.isBlank()) {
            try {
                String rawSr = (srNo != null) ? srNo.trim() : "";
                String cleanSr = rawSr.contains("/") ? rawSr.substring(rawSr.lastIndexOf('/') + 1).trim() : rawSr;
                String digits = cleanSr.replaceAll("[^0-9]", "");

                List<String> uomList = jdbcTemplate.query(
                    "SELECT pi.uom FROM po_item pi JOIN po_header ph ON pi.po_header_id = ph.id " +
                    "WHERE ph.po_no = ? AND (" +
                    "   pi.item_sr_no = ? " +
                    "   OR pi.item_sr_no = ? " +
                    "   OR pi.item_sr_no = LPAD(?, 3, '0') " +
                    (!digits.isEmpty() ? "   OR (pi.item_sr_no REGEXP '^[0-9]+$' AND CAST(pi.item_sr_no AS UNSIGNED) = " + Integer.parseInt(digits) + ")" : "") +
                    ") LIMIT 1",
                    (rs, rowNum) -> rs.getString("uom"),
                    poNo.trim(), rawSr, cleanSr, digits.isEmpty() ? cleanSr : digits
                );
                if (uomList != null && !uomList.isEmpty() && uomList.get(0) != null && !uomList.get(0).isBlank()) {
                    uom = uomList.get(0).trim();
                } else {
                    List<String> fallbackList = jdbcTemplate.query(
                        "SELECT pi.uom FROM po_item pi JOIN po_header ph ON pi.po_header_id = ph.id WHERE ph.po_no = ? LIMIT 1",
                        (rs, rowNum) -> rs.getString("uom"),
                        poNo.trim()
                    );
                    if (fallbackList != null && !fallbackList.isEmpty() && fallbackList.get(0) != null && !fallbackList.get(0).isBlank()) {
                        uom = fallbackList.get(0).trim();
                    }
                }
            } catch (Exception ignored) {}
        }

        if (uom == null || uom.isBlank()) {
            String st = sleeperType != null ? sleeperType.toUpperCase() : "";
            if (st.contains("SET") || st.contains("PNC") || st.contains("TURNOUT") || st.contains("4218") || st.contains("4865") || st.contains("9790") || st.contains("4732") || st.contains("DERAIL")) {
                uom = "Set";
            } else {
                uom = "Nos.";
            }
        }
        return uom;
    }
}
