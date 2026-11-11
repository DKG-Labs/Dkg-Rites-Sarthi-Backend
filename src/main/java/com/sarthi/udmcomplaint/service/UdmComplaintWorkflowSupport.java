package com.sarthi.udmcomplaint.service;

import com.sarthi.constant.AppConstant;
import com.sarthi.entity.TransitionMaster;
import com.sarthi.entity.WorkflowMaster;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.repository.TransitionMasterRepository;
import com.sarthi.repository.WorkflowMasterRepository;
import com.sarthi.udmcomplaint.dto.UdmComplaintStatusDto;
import com.sarthi.udmcomplaint.entity.UdmComplaint;
import com.sarthi.udmcomplaint.entity.UdmComplaintWorkflowTransition;
import com.sarthi.udmcomplaint.repository.UdmComplaintRepository;
import com.sarthi.udmcomplaint.repository.UdmComplaintWorkflowTransitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import static com.sarthi.udmcomplaint.UdmComplaintConstants.REMARKS_MAX;
import static com.sarthi.udmcomplaint.UdmComplaintConstants.WORKFLOW_NAME;

/** Shared helpers for the UDM complaint services (transition lookup, save, history, validation). */
@Component
@RequiredArgsConstructor
public class UdmComplaintWorkflowSupport {

    private final UdmComplaintRepository complaintRepository;
    private final UdmComplaintWorkflowTransitionRepository transitionLogRepository;
    private final WorkflowMasterRepository workflowMasterRepository;
    private final TransitionMasterRepository transitionMasterRepository;

    public UdmComplaint save(UdmComplaint c) {
        try {
            return complaintRepository.saveAndFlush(c);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw validation("This complaint was just updated by someone else. Please refresh and try again.");
        }
    }

    public TransitionMaster transition(String name) {
        WorkflowMaster wf = workflowMasterRepository.findByWorkflowName(WORKFLOW_NAME);
        if (wf == null) throw validation("Workflow " + WORKFLOW_NAME + " is not configured. Run migration V20261111_001.");
        return transitionMasterRepository.findByWorkflowId(wf.getWorkflowId()).stream()
                .filter(t -> name.equalsIgnoreCase(t.getTransitionName()))
                .findFirst()
                .orElseThrow(() -> validation("Transition " + name + " is not configured for " + WORKFLOW_NAME + "."));
    }

    public void logTransition(UdmComplaint c, TransitionMaster tr, String fromStatus, String toStatus,
                              String fromRole, String toRole, Integer toRoleId,
                              String remarks, String rio, Integer assignedTo, Integer actionBy) {
        UdmComplaintWorkflowTransition t = new UdmComplaintWorkflowTransition();
        t.setComplaintId(c.getId());
        t.setUdmRejectionId(c.getUdmRejectionId());
        t.setRejectionNumber(c.getRejectionNumber());
        t.setWorkflowId(tr.getWorkflowId());
        t.setTransitionId(tr.getTransitionId());
        t.setAction(tr.getTransitionName());
        t.setCurrentRoleId(tr.getCurrentRoleId());
        t.setCurrentRoleName(fromRole);
        t.setNextRoleId(toRoleId != null ? toRoleId : tr.getNextRoleId());
        t.setNextRoleName(toRole);
        t.setCurrentStatus(fromStatus);
        t.setNextStatus(toStatus);
        t.setRemarks(remarks);
        t.setAssignedRio(rio);
        t.setAssignedToUser(assignedTo);
        t.setCreatedBy(actionBy);
        transitionLogRepository.save(t);
    }

    public String trimRemarks(String remarks, boolean mandatory) {
        String r = remarks == null ? "" : remarks.trim();
        if (mandatory && r.isEmpty()) throw validation("Remarks are required.");
        if (r.length() > REMARKS_MAX) throw validation("Remarks must be at most " + REMARKS_MAX + " characters.");
        return r.isEmpty() ? null : r;
    }

    public UdmComplaintStatusDto toStatusDto(UdmComplaint c) {
        UdmComplaintStatusDto d = new UdmComplaintStatusDto();
        d.setUdmRejectionId(c.getUdmRejectionId());
        d.setRejectionNumber(c.getRejectionNumber());
        d.setComplaintStatus(c.getStatus());
        d.setPendingWith(c.getPendingWithRoleName());
        d.setAssignedRio(c.getAssignedRio());
        d.setAssignedCmUserId(c.getAssignedCmUserId());
        d.setAssignedCmName(c.getAssignedCmName());
        d.setLastRemarks(c.getLastRemarks());
        d.setClosedAt(c.getClosedAt() != null ? c.getClosedAt().toString() : null);
        d.setUpdatedAt(c.getUpdatedAt() != null ? c.getUpdatedAt().toString()
                : (c.getCreatedAt() != null ? c.getCreatedAt().toString() : null));
        return d;
    }

    public static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    public static BusinessException validation(String message) {
        return new BusinessException(new ErrorDetails(
                AppConstant.ERROR_CODE_RESOURCE,
                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                AppConstant.ERROR_TYPE_VALIDATION,
                message));
    }
}
