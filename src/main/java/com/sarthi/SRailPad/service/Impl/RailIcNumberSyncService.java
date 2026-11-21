package com.sarthi.SRailPad.service.Impl;

import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCompleteDetails;
import com.sarthi.SRailPad.repository.RailWorkflowTransactionRepository;
import com.sarthi.SRailPad.repository.inspectionCall.RailInspectionCompleteDetailsRepository;
import com.sarthi.SRailPad.repository.inspectionCall.RailpadFinalIcEditRepository;
import com.sarthi.SRailPad.repository.inspectionCall.RailpadFinalIcSaveChangesRepository;
import com.sarthi.SRailPad.repository.inspectionCall.RailpadProcessIcEditRepository;
import com.sarthi.SRailPad.repository.inspectionCall.RailpadProcessIcSaveChangesRepository;
import com.sarthi.entity.UserMaster;
import com.sarthi.repository.UserMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rail Pad IC number is "R/CALL/IE" (IE = short name). It is created at FINISH from whoever finished the call.
 * If the call is then reassigned, the IC must carry the IE who now issues it, so until the IC is generated
 * the IE part follows the IE currently assigned to the call. After Generate IC / sign / IBS it never changes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RailIcNumberSyncService {

    private final RailWorkflowTransactionRepository workflowRepository;
    private final RailInspectionCompleteDetailsRepository completeDetailsRepository;
    private final RailpadProcessIcEditRepository processIcEditRepository;
    private final RailpadProcessIcSaveChangesRepository processIcSaveChangesRepository;
    private final RailpadFinalIcEditRepository finalIcEditRepository;
    private final RailpadFinalIcSaveChangesRepository finalIcSaveChangesRepository;
    private final UserMasterRepository userMasterRepository;

    /** Returns the IC number after any correction (or the stored one if nothing changed). */
    @Transactional
    public String syncIeSuffix(String callNo) {
        if (callNo == null || callNo.isBlank()) return null;
        String call = callNo.trim();

        RailInspectionCompleteDetails det = completeDetailsRepository.findFirstByCallNoOrderByCreatedOnDesc(call).orElse(null);
        if (det == null || det.getCertificateNo() == null || det.getCertificateNo().isBlank()) return null; // not finished yet
        String oldNo = det.getCertificateNo().trim();

        // IC already generated / signed / sent to IBS: never rename automatically
        if (workflowRepository.countIcIssuedActions(call) > 0) return oldNo;

        Long ieId = workflowRepository.findLatestAssignedUser(call);
        if (ieId == null) return oldNo;
        UserMaster ie = userMasterRepository.findById(Math.toIntExact(ieId)).orElse(null);
        if (ie == null || ie.getShortName() == null || ie.getShortName().isBlank()) return oldNo;

        String[] parts = oldNo.split("/");
        if (parts.length < 3) return oldNo;
        String newNo = parts[0] + "/" + String.join("/", java.util.Arrays.copyOfRange(parts, 1, parts.length - 1))
                + "/" + ie.getShortName().trim().toUpperCase();
        if (newNo.equalsIgnoreCase(oldNo)) return oldNo;

        det.setCertificateNo(newNo);
        completeDetailsRepository.save(det);
        int moved = processIcEditRepository.renameIcNumber(oldNo, newNo)
                + processIcSaveChangesRepository.renameIcNumber(oldNo, newNo)
                + finalIcEditRepository.renameIcNumber(oldNo, newNo)
                + finalIcSaveChangesRepository.renameIcNumber(oldNo, newNo);
        log.info("[Railpad] IC number {} -> {} (call {} now with user {}); {} draft row(s) moved", oldNo, newNo, call, ieId, moved);
        return newNo;
    }
}
