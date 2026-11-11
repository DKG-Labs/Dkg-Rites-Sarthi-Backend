package com.sarthi.udmcomplaint.service;

import com.sarthi.entity.TransitionMaster;
import com.sarthi.udmcomplaint.dto.RbContextDto;
import com.sarthi.udmcomplaint.dto.RbReviewRequest;
import com.sarthi.udmcomplaint.dto.UdmComplaintStatusDto;
import com.sarthi.udmcomplaint.entity.UdmComplaint;
import com.sarthi.udmcomplaint.entity.UdmComplaintStage;
import com.sarthi.udmcomplaint.repository.UdmComplaintRepository;
import com.sarthi.udmcomplaint.repository.UdmComplaintStageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import static com.sarthi.udmcomplaint.UdmComplaintConstants.*;
import static com.sarthi.udmcomplaint.service.UdmComplaintWorkflowSupport.round;
import static com.sarthi.udmcomplaint.service.UdmComplaintWorkflowSupport.validation;

/**
 * Railway Board review. Any user holding the Railway Board role (user_role_master) may act on
 * complaints with status SENT_TO_RAILWAY_BOARD. The mock "Railwayboard" login has no user id and cannot act.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UdmComplaintRbServiceImpl implements UdmComplaintRbService {

    private final UdmComplaintRepository complaintRepository;
    private final UdmComplaintStageRepository stageRepository;
    private final UdmComplaintWorkflowSupport support;
    private final UdmRoleResolver roles;

    @Override
    @Transactional(readOnly = true)
    public RbContextDto getRbContext(Long udmRejectionId, Integer userId) {
        RbContextDto d = new RbContextDto();
        Optional<UdmComplaint> oc = complaintRepository.findByUdmRejectionId(udmRejectionId);
        String status = oc.map(UdmComplaint::getStatus).orElse(ST_PENDING_CC);
        d.setComplaintStatus(status);
        d.setCanAct(oc.isPresent() && ST_SENT_TO_RB.equals(status) && support.hasAnyRole(userId, RAILWAY_BOARD_ROLE_NAMES));
        oc.ifPresent(c -> d.setCurrentRound(round(c)));
        return d;
    }

    @Override
    @Transactional
    public UdmComplaintStatusDto review(Long udmRejectionId, RbReviewRequest req, Integer userId) {
        if (userId == null) throw validation("User not identified.");
        if (!support.hasAnyRole(userId, RAILWAY_BOARD_ROLE_NAMES)) throw validation("Only the Railway Board can perform this action.");
        if (req == null) throw validation("Please choose Approve or Return to SBU Head.");

        UdmComplaint c = complaintRepository.findByUdmRejectionId(udmRejectionId)
                .orElseThrow(() -> validation("Complaint not found."));
        if (!ST_SENT_TO_RB.equals(c.getStatus())) {
            throw validation("This complaint is not pending with Railway Board (status " + c.getStatus() + "). Please refresh.");
        }

        String decision = req.getDecision() == null ? "" : req.getDecision().trim().toUpperCase(Locale.ROOT);
        boolean approve = DECISION_APPROVE.equals(decision);
        if (!approve && !DECISION_RETURN.equals(decision)) {
            throw validation("Please choose Approve or Return to SBU Head.");
        }
        // SRS: remarks mandatory when returning
        String remarks = support.trimRemarks(req.getRemarks(), !approve);

        UdmComplaintStage s = new UdmComplaintStage();
        s.setComplaintId(c.getId());
        s.setUdmRejectionId(c.getUdmRejectionId());
        s.setStage(STAGE_RB_REVIEW);
        s.setRoundNo(round(c));
        s.setPath(decision);
        s.setRemarks(remarks);
        s.setCreatedBy(userId);
        stageRepository.save(s);

        TransitionMaster tr = support.transition(approve ? TR_RB_APPROVE : TR_RB_RETURN_TO_SBU);
        String fromStatus = c.getStatus();
        String toStatus = approve ? ST_CLOSED : ST_RETURNED_TO_SBU;
        String toRoleName = approve ? null : ROLE_SBU_HEAD_NAME;
        Integer toRoleId = approve ? null : roles.sbuHeadRoleId();

        c.setStatus(toStatus);
        c.setPendingWithRoleId(toRoleId);
        c.setPendingWithRoleName(toRoleName);
        if (remarks != null) c.setLastRemarks(remarks);
        if (approve) {
            c.setClosedBy(userId);
            c.setClosedAt(LocalDateTime.now());
        }
        c.setUpdatedBy(userId);
        c = support.save(c);   // optimistic lock: two Railway Board users cannot both act

        support.logTransition(c, tr, fromStatus, toStatus, ROLE_RAILWAY_BOARD_NAME, toRoleName, toRoleId,
                remarks, c.getAssignedRio(), null, userId);
        log.info("[UDM-COMPLAINT] {} Railway Board {} by user {} ({} -> {})", c.getRejectionNumber(), decision, userId, fromStatus, toStatus);
        return support.toStatusDto(c);
    }
}
