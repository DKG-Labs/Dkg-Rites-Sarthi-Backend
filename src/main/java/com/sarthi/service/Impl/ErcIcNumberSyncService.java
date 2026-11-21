package com.sarthi.service.Impl;

import com.sarthi.entity.InspectionCompleteDetails;
import com.sarthi.entity.UserMaster;
import com.sarthi.repository.InspectionCompleteDetailsRepository;
import com.sarthi.repository.UserMasterRepository;
import com.sarthi.repository.WorkflowTransitionRepository;
import com.sarthi.repository.finalmaterial.FinalIcEditRepository;
import com.sarthi.repository.finalmaterial.FinalIcSaveChangesRepository;
import com.sarthi.repository.processmaterial.ProcessIcEditRepository;
import com.sarthi.repository.processmaterial.ProcessIcSaveChangesRepository;
import com.sarthi.repository.rawmaterial.RmIcEditRepository;
import com.sarthi.repository.rawmaterial.RmIcSaveChangesRepository;
import com.sarthi.util.IcNumberSuffix;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ERC (RM / Process / Final): the IC number "R/CALL/IE" is created at FINISH with the finishing IE's name.
 * If the call is reassigned before the IC is issued, the IE part follows the IE now handling the call.
 * After Generate IC / sign / IBS it is never changed.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ErcIcNumberSyncService {

    private final WorkflowTransitionRepository workflowRepository;
    private final InspectionCompleteDetailsRepository completeDetailsRepository;
    private final UserMasterRepository userMasterRepository;
    private final RmIcEditRepository rmIcEditRepository;
    private final RmIcSaveChangesRepository rmIcSaveChangesRepository;
    private final ProcessIcEditRepository processIcEditRepository;
    private final ProcessIcSaveChangesRepository processIcSaveChangesRepository;
    private final FinalIcEditRepository finalIcEditRepository;
    private final FinalIcSaveChangesRepository finalIcSaveChangesRepository;

    @Transactional
    public String syncIeSuffix(String callNo) {
        if (callNo == null || callNo.isBlank()) return null;
        String call = callNo.trim();

        InspectionCompleteDetails det = completeDetailsRepository.findFirstByCallNoOrderByCreatedOnDesc(call).orElse(null);
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
        int moved = rmIcEditRepository.renameIcNumber(oldNo, newNo)
                + rmIcSaveChangesRepository.renameIcNumber(oldNo, newNo)
                + processIcEditRepository.renameIcNumber(oldNo, newNo)
                + processIcSaveChangesRepository.renameIcNumber(oldNo, newNo)
                + finalIcEditRepository.renameIcNumber(oldNo, newNo)
                + finalIcSaveChangesRepository.renameIcNumber(oldNo, newNo);
        log.info("[ERC] IC number {} -> {} (call {} now with user {}); {} draft row(s) moved",
                oldNo, newNo, call, assigned.get(0), moved);
        return newNo;
    }
}
