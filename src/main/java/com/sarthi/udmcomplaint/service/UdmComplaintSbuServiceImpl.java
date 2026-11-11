package com.sarthi.udmcomplaint.service;

import com.sarthi.entity.TransitionMaster;
import com.sarthi.udmcomplaint.dto.SbuContextDto;
import com.sarthi.udmcomplaint.dto.SbuReviewRequest;
import com.sarthi.udmcomplaint.dto.UdmComplaintStatusDto;
import com.sarthi.udmcomplaint.entity.UdmComplaint;
import com.sarthi.udmcomplaint.entity.UdmComplaintStage;
import com.sarthi.udmcomplaint.repository.UdmComplaintRepository;
import com.sarthi.udmcomplaint.repository.UdmComplaintStageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static com.sarthi.udmcomplaint.UdmComplaintConstants.*;
import static com.sarthi.udmcomplaint.service.UdmComplaintWorkflowSupport.round;
import static com.sarthi.udmcomplaint.service.UdmComplaintWorkflowSupport.validation;

/**
 * SBU Head review. Any user holding the SBU Head role (user_role_master) may act on complaints
 * pending with SBU Head (CAPA submitted, or returned by Railway Board).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UdmComplaintSbuServiceImpl implements UdmComplaintSbuService {

    /** Statuses on which the SBU Head can act. */
    private static final Set<String> SBU_PENDING = Set.of(ST_CAPA_SUBMITTED, ST_RETURNED_TO_SBU);

    private final UdmComplaintRepository complaintRepository;
    private final UdmComplaintStageRepository stageRepository;
    private final UdmComplaintWorkflowSupport support;
    private final UdmRoleResolver roles;

    @Override
    @Transactional(readOnly = true)
    public SbuContextDto getSbuContext(Long udmRejectionId, Integer userId) {
        SbuContextDto d = new SbuContextDto();
        Optional<UdmComplaint> oc = complaintRepository.findByUdmRejectionId(udmRejectionId);
        String status = oc.map(UdmComplaint::getStatus).orElse(ST_PENDING_CC);
        d.setComplaintStatus(status);
        d.setCanAct(oc.isPresent() && SBU_PENDING.contains(status) && support.hasAnyRole(userId, SBU_HEAD_ROLE_NAMES));
        oc.ifPresent(c -> {
            d.setCurrentRound(round(c));
            if (ST_RETURNED_TO_SBU.equals(c.getStatus())) d.setReturnedRemarks(c.getLastRemarks());
        });
        return d;
    }

    @Override
    @Transactional
    public UdmComplaintStatusDto review(Long udmRejectionId, SbuReviewRequest req, Integer userId) {
        if (userId == null) throw validation("User not identified.");
        if (!support.hasAnyRole(userId, SBU_HEAD_ROLE_NAMES)) throw validation("Only the SBU Head can perform this action.");
        if (req == null) throw validation("Please choose Forward to Railway Board or Return to CM.");

        UdmComplaint c = complaintRepository.findByUdmRejectionId(udmRejectionId)
                .orElseThrow(() -> validation("Complaint not found."));
        if (!SBU_PENDING.contains(c.getStatus())) {
            throw validation("This complaint is not pending with SBU Head (status " + c.getStatus() + "). Please refresh.");
        }

        String decision = req.getDecision() == null ? "" : req.getDecision().trim().toUpperCase(Locale.ROOT);
        boolean forward = DECISION_FORWARD.equals(decision);
        if (!forward && !DECISION_RETURN.equals(decision)) {
            throw validation("Please choose Forward to Railway Board or Return to CM.");
        }
        // SRS: remarks mandatory when returning
        String remarks = support.trimRemarks(req.getRemarks(), !forward);

        UdmComplaintStage s = new UdmComplaintStage();
        s.setComplaintId(c.getId());
        s.setUdmRejectionId(c.getUdmRejectionId());
        s.setStage(STAGE_SBU_REVIEW);
        s.setRoundNo(round(c));
        s.setPath(decision);
        s.setRemarks(remarks);
        s.setCreatedBy(userId);
        stageRepository.save(s);

        TransitionMaster tr = support.transition(forward ? TR_SBU_FORWARD_TO_RB : TR_SBU_RETURN_TO_CM);
        String fromStatus = c.getStatus();
        String toStatus = forward ? ST_SENT_TO_RB : ST_RETURNED_TO_CM;
        String toRoleName = forward ? ROLE_RAILWAY_BOARD_NAME : ROLE_CM_NAME;
        Integer toRoleId = forward ? roles.railwayBoardRoleId() : roles.cmRoleId();

        c.setStatus(toStatus);
        c.setPendingWithRoleId(toRoleId);
        c.setPendingWithRoleName(toRoleName);
        if (remarks != null) c.setLastRemarks(remarks);
        if (!forward) c.setCurrentRound(round(c) + 1);   // CM resubmits RCA + CAPA as the next round
        c.setUpdatedBy(userId);
        c = support.save(c);   // optimistic lock: two SBU Heads cannot both act

        support.logTransition(c, tr, fromStatus, toStatus, ROLE_SBU_HEAD_NAME, toRoleName, toRoleId,
                remarks, c.getAssignedRio(), forward ? null : c.getAssignedCmUserId(), userId);
        log.info("[UDM-COMPLAINT] {} SBU {} by user {} ({} -> {})", c.getRejectionNumber(), decision, userId, fromStatus, toStatus);
        return support.toStatusDto(c);
    }
}
