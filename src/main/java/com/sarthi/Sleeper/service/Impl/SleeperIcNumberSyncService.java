package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCompleteDetails;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.SleeperFinalIcEditRepository;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.SleeperFinalIcSaveChangesRepository;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.SleeperInspectionCompleteDetailsRepository;
import com.sarthi.Sleeper.repository.SleeperWorkflowRepository;
import com.sarthi.entity.UserMaster;
import com.sarthi.repository.UserMasterRepository;
import com.sarthi.util.IcNumberSuffix;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Sleeper Final: the IC number "R/CALL/IE" is created at FINISH with the finishing IE's name.
 * If the call is reassigned before the IC is issued, the IE part follows the IE now handling the call.
 * After Generate IC / sign / IBS it is never changed.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SleeperIcNumberSyncService {

    private final SleeperWorkflowRepository workflowRepository;
    private final SleeperInspectionCompleteDetailsRepository completeDetailsRepository;
    private final UserMasterRepository userMasterRepository;
    private final SleeperFinalIcEditRepository finalIcEditRepository;
    private final SleeperFinalIcSaveChangesRepository finalIcSaveChangesRepository;

    @Transactional
    public String syncIeSuffix(String callNo) {
        if (callNo == null || callNo.isBlank()) return null;
        String call = callNo.trim();

        SleeperInspectionCompleteDetails det = completeDetailsRepository.findFirstByCallNoOrderByCreatedOnDesc(call).orElse(null);
        if (det == null || det.getCertificateNo() == null || det.getCertificateNo().isBlank()) return null; // not finished yet
        String oldNo = det.getCertificateNo().trim();

        if (workflowRepository.countIcIssuedActions(call) > 0) return oldNo; // IC already issued

        List<Number> assigned = workflowRepository.findAssignedUsersLatestFirst(call);
        if (assigned.isEmpty()) return oldNo;
        UserMaster ie = userMasterRepository.findById(assigned.get(0).intValue()).orElse(null);
        String newNo = ie == null ? null : IcNumberSuffix.withIe(oldNo, ie.getShortName());
        if (newNo == null || newNo.equalsIgnoreCase(oldNo)) return oldNo;

        det.setCertificateNo(newNo);
        completeDetailsRepository.save(det);
        int moved = finalIcEditRepository.renameIcNumber(oldNo, newNo)
                + finalIcSaveChangesRepository.renameIcNumber(oldNo, newNo);
        log.info("[Sleeper] IC number {} -> {} (call {} now with user {}); {} draft row(s) moved",
                oldNo, newNo, call, assigned.get(0), moved);
        return newNo;
    }
}
