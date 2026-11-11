package com.sarthi.udmcomplaint.service;

import com.sarthi.udmcomplaint.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/** Phase 2 — Controlling Manager stages: Decision, Complete JI / Audit, RCA, CAPA; documents. */
public interface UdmComplaintCmService {

    /** Status, whether the logged-in user may act, next form, 90-day flag and dropdown values. */
    CmContextDto getCmContext(Long udmRejectionId, Integer userId);

    /** JI team choices: CMs of the complaint's RIO + all IEs (ERC / Sleeper / Rail Pad, no Process IE). */
    List<JiTeamOptionDto> getJiTeamOptions(Long udmRejectionId, Integer userId);

    UdmComplaintStatusDto decide(Long udmRejectionId, CmDecisionRequest request, Integer userId);

    UdmComplaintStatusDto completeJi(Long udmRejectionId, CompleteJiRequest request, MultipartFile report, Integer userId);

    UdmComplaintStatusDto completeAudit(Long udmRejectionId, CompleteAuditRequest request, MultipartFile report, Integer userId);

    UdmComplaintStatusDto submitRca(Long udmRejectionId, RcaRequest request, MultipartFile evidence, Integer userId);

    UdmComplaintStatusDto submitCapa(Long udmRejectionId, CapaRequest request, MultipartFile evidence, Integer userId);

    List<UdmComplaintStageDto> getStages(Long udmRejectionId);

    List<UdmComplaintDocumentDto> getDocuments(Long udmRejectionId);

    UdmDocumentFile downloadDocument(Long documentId);

    Map<String, List<String>> getDropdowns();
}
