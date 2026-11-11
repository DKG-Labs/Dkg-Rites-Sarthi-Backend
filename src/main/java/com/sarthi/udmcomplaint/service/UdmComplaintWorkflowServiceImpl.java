package com.sarthi.udmcomplaint.service;

import com.sarthi.entity.TransitionMaster;
import com.sarthi.entity.UdmRejection;
import com.sarthi.repository.UdmRejectionRepository;
import com.sarthi.udmcomplaint.dto.*;
import com.sarthi.udmcomplaint.entity.UdmComplaint;
import com.sarthi.udmcomplaint.repository.UdmComplaintRepository;
import com.sarthi.udmcomplaint.repository.UdmComplaintWorkflowTransitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.sarthi.udmcomplaint.UdmComplaintConstants.*;
import static com.sarthi.udmcomplaint.service.UdmComplaintWorkflowSupport.isBlank;
import static com.sarthi.udmcomplaint.service.UdmComplaintWorkflowSupport.validation;

@Slf4j
@Service
@RequiredArgsConstructor
public class UdmComplaintWorkflowServiceImpl implements UdmComplaintWorkflowService {

    private final UdmComplaintRepository complaintRepository;
    private final UdmComplaintWorkflowTransitionRepository transitionLogRepository;
    private final UdmRejectionRepository udmRejectionRepository;
    private final UdmComplaintWorkflowSupport support;

    // ------------------------------------------------------------------ actions

    @Override
    @Transactional
    public UdmComplaintStatusDto sendToCm(Long udmRejectionId, SendToCmRequest req, Integer actionBy) {
        requireRole(actionBy, ROLE_COMPLAINT_CELL);
        if (req == null || isBlank(req.getRio())) throw validation("Please select a RIO.");
        if (req.getCmUserId() == null) throw validation("Please select a Controlling Manager.");
        String remarks = support.trimRemarks(req.getRemarks(), false);

        String rio = req.getRio().trim();
        if (complaintRepository.countCmInRio(req.getCmUserId(), rio, ROLE_CM_ID) == 0) {
            throw validation("Selected Controlling Manager does not belong to RIO " + rio + ".");
        }
        String cmName = complaintRepository.findCmsByRio(rio, ROLE_CM_ID).stream()
                .filter(r -> r[0] != null && ((Number) r[0]).intValue() == req.getCmUserId())
                .map(r -> r[1] != null ? r[1].toString() : null)
                .findFirst().orElse(null);

        UdmComplaint c = loadOrCreate(udmRejectionId, actionBy);
        if (ST_CLOSED.equals(c.getStatus())) throw validation("Complaint is already closed.");

        TransitionMaster tr = support.transition(TR_SEND_TO_CM);
        String fromStatus = c.getStatus();

        c.setStatus(ST_SENT_TO_CM);
        c.setPendingWithRoleId(ROLE_CM_ID);
        c.setPendingWithRoleName(ROLE_CM_NAME);
        c.setAssignedRio(rio);
        c.setAssignedCmUserId(req.getCmUserId());
        c.setAssignedCmName(cmName);
        c.setLastRemarks(remarks);
        c.setUpdatedBy(actionBy);
        c = support.save(c);

        support.logTransition(c, tr, fromStatus, ST_SENT_TO_CM, ROLE_COMPLAINT_CELL, ROLE_CM_NAME, ROLE_CM_ID,
                remarks, rio, req.getCmUserId(), actionBy);
        log.info("[UDM-COMPLAINT] {} sent to CM {} ({}) by user {}", c.getRejectionNumber(), req.getCmUserId(), rio, actionBy);
        return support.toStatusDto(c);
    }

    @Override
    @Transactional
    public UdmComplaintStatusDto close(Long udmRejectionId, CloseComplaintRequest req, Integer actionBy) {
        requireRole(actionBy, ROLE_COMPLAINT_CELL);
        String remarks = support.trimRemarks(req != null ? req.getRemarks() : null, true);

        UdmComplaint c = loadOrCreate(udmRejectionId, actionBy);
        if (ST_CLOSED.equals(c.getStatus())) throw validation("Complaint is already closed.");

        TransitionMaster tr = support.transition(TR_CLOSE);
        String fromStatus = c.getStatus();

        c.setStatus(ST_CLOSED);
        c.setPendingWithRoleId(null);
        c.setPendingWithRoleName(null);
        c.setLastRemarks(remarks);
        c.setClosedBy(actionBy);
        c.setClosedAt(LocalDateTime.now());
        c.setUpdatedBy(actionBy);
        c = support.save(c);

        support.logTransition(c, tr, fromStatus, ST_CLOSED, ROLE_COMPLAINT_CELL, null, null,
                remarks, c.getAssignedRio(), null, actionBy);
        log.info("[UDM-COMPLAINT] {} closed by user {}", c.getRejectionNumber(), actionBy);
        return support.toStatusDto(c);
    }

    // ------------------------------------------------------------------ lookups

    @Override
    @Transactional(readOnly = true)
    public List<String> getRios() {
        return complaintRepository.findRiosHavingCm(ROLE_CM_ID);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CmOptionDto> getCmsByRio(String rio) {
        if (isBlank(rio)) return List.of();
        return complaintRepository.findCmsByRio(rio.trim(), ROLE_CM_ID).stream()
                .map(r -> new CmOptionDto(
                        r[0] != null ? ((Number) r[0]).intValue() : null,
                        r[1] != null ? r[1].toString() : "",
                        r[2] != null ? r[2].toString() : ""))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UdmComplaintStatusDto> getAssignedToCm(Integer cmUserId, int page, int size) {
        if (cmUserId == null) throw validation("User not identified.");
        int safeSize = Math.min(Math.max(size, 1), 100);
        return complaintRepository
                .findByAssignedCmUserIdOrderByUpdatedAtDescCreatedAtDesc(cmUserId, PageRequest.of(Math.max(page, 0), safeSize))
                .map(support::toStatusDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UdmComplaintHistoryDto> getHistory(Long udmRejectionId) {
        return transitionLogRepository.findByUdmRejectionIdOrderByCreatedDateAsc(udmRejectionId).stream()
                .map(t -> {
                    UdmComplaintHistoryDto d = new UdmComplaintHistoryDto();
                    d.setAction(t.getAction());
                    d.setFromRole(t.getCurrentRoleName());
                    d.setToRole(t.getNextRoleName());
                    d.setStatus(t.getNextStatus());
                    d.setRemarks(t.getRemarks());
                    d.setAssignedRio(t.getAssignedRio());
                    d.setAssignedToUser(t.getAssignedToUser());
                    d.setActionBy(t.getCreatedBy());
                    d.setActionDate(t.getCreatedDate() != null ? t.getCreatedDate().toString() : null);
                    return d;
                }).toList();
    }

    // ------------------------------------------------------------------ helpers

    /** Existing complaint row, or a new one (with a RECEIVED entry in the trail) for a never-actioned rejection. */
    private UdmComplaint loadOrCreate(Long udmRejectionId, Integer actionBy) {
        if (udmRejectionId == null) throw validation("Rejection id is required.");
        return complaintRepository.findByUdmRejectionId(udmRejectionId).orElseGet(() -> {
            UdmRejection r = udmRejectionRepository.findById(udmRejectionId)
                    .orElseThrow(() -> validation("UDM rejection not found: " + udmRejectionId));
            UdmComplaint c = new UdmComplaint();
            c.setUdmRejectionId(r.getId());
            c.setRejectionNumber(r.getRejectionNumber());
            c.setStatus(ST_PENDING_CC);
            c.setPendingWithRoleName(ROLE_COMPLAINT_CELL);
            c.setCreatedBy(actionBy);
            c = complaintRepository.saveAndFlush(c);
            support.logTransition(c, support.transition(TR_RECEIVED), null, ST_PENDING_CC, null, ROLE_COMPLAINT_CELL, null,
                    "Received from UDM", null, null, actionBy);
            return c;
        });
    }

    /** Permission is checked from the logged-in user's roles in user_role_master, never from the request. */
    private void requireRole(Integer userId, String roleName) {
        if (userId == null) throw validation("User not identified.");
        boolean ok = complaintRepository.findRoleNamesByUserId(userId).stream()
                .anyMatch(r -> r != null && r.trim().equalsIgnoreCase(roleName));
        if (!ok) throw validation("Only " + roleName + " can perform this action.");
    }
}
