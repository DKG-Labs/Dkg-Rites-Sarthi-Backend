package com.sarthi.udmcomplaint.service;

import com.sarthi.entity.TransitionMaster;
import com.sarthi.entity.UdmRejection;
import com.sarthi.repository.UdmRejectionRepository;
import com.sarthi.udmcomplaint.dto.*;
import com.sarthi.udmcomplaint.entity.UdmComplaint;
import com.sarthi.udmcomplaint.entity.UdmComplaintDocument;
import com.sarthi.udmcomplaint.entity.UdmComplaintStage;
import com.sarthi.udmcomplaint.repository.UdmComplaintDocumentRepository;
import com.sarthi.udmcomplaint.repository.UdmComplaintDropdownRepository;
import com.sarthi.udmcomplaint.repository.UdmComplaintRepository;
import com.sarthi.udmcomplaint.repository.UdmComplaintStageRepository;
import com.sarthi.udmcomplaint.service.UdmComplaintDocumentStorage.ValidPdf;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.sarthi.udmcomplaint.UdmComplaintConstants.*;
import static com.sarthi.udmcomplaint.service.UdmComplaintWorkflowSupport.isBlank;
import static com.sarthi.udmcomplaint.service.UdmComplaintWorkflowSupport.validation;

/**
 * Controlling Manager stages (SRS §5, stages 3–5).
 * Only the CM the complaint is assigned to may act, and only on the form that matches the current status.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UdmComplaintCmServiceImpl implements UdmComplaintCmService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final String LIST_SEP = "|";

    /** Status → the one form the CM may submit next. */
    private static final Map<String, String> NEXT_STAGE = Map.of(
            ST_SENT_TO_CM, STAGE_DECISION,
            ST_JI_SCHEDULED, STAGE_JI_COMPLETE,
            ST_AUDIT_IN_PROGRESS, STAGE_AUDIT_COMPLETE,
            ST_JI_COMPLETED, STAGE_RCA,
            ST_AUDIT_COMPLETED, STAGE_RCA,
            ST_RCA_SUBMITTED, STAGE_CAPA);

    private final UdmComplaintRepository complaintRepository;
    private final UdmComplaintStageRepository stageRepository;
    private final UdmComplaintDocumentRepository documentRepository;
    private final UdmComplaintDropdownRepository dropdownRepository;
    private final UdmRejectionRepository udmRejectionRepository;
    private final UdmComplaintWorkflowSupport support;
    private final UdmComplaintDocumentStorage storage;

    // ------------------------------------------------------------------ context

    @Override
    @Transactional(readOnly = true)
    public CmContextDto getCmContext(Long udmRejectionId, Integer userId) {
        CmContextDto d = new CmContextDto();
        Optional<UdmComplaint> oc = complaintRepository.findByUdmRejectionId(udmRejectionId);
        String status = oc.map(UdmComplaint::getStatus).orElse(ST_PENDING_CC);
        d.setComplaintStatus(status);
        d.setNextStage(NEXT_STAGE.get(status));
        d.setCanAct(oc.isPresent() && d.getNextStage() != null && isAssignedCm(oc.get(), userId));

        udmRejectionRepository.findById(udmRejectionId).ifPresent(r -> {
            Long days = daysToFailure(r);
            d.setDaysToFailure(days);
            d.setFailedWithin90Days(days == null ? null : days >= 0 && days <= FAILED_WITHIN_DAYS);
        });
        d.setDropdowns(getDropdowns());
        return d;
    }

    // ------------------------------------------------------------------ stage 3: decision

    @Override
    @Transactional
    public UdmComplaintStatusDto decide(Long udmRejectionId, CmDecisionRequest req, Integer userId) {
        if (req == null) throw validation("Please choose Joint Inspection or Audit.");
        UdmComplaint c = loadForCm(udmRejectionId, userId, STAGE_DECISION);
        String path = req.getPath() == null ? "" : req.getPath().trim().toUpperCase(Locale.ROOT);
        String remarks = support.trimRemarks(req.getRemarks(), false);

        UdmComplaintStage s = newStage(c, STAGE_DECISION, userId);
        s.setFlagFailed90(Boolean.TRUE.equals(req.getFlagFailed90()));
        s.setFlagQtyOver5(Boolean.TRUE.equals(req.getFlagQtyOver5()));
        s.setFlagCritical(Boolean.TRUE.equals(req.getFlagCritical()));
        s.setRemarks(remarks);

        String transition;
        String toStatus;
        if (PATH_JI.equals(path)) {
            s.setPath(PATH_JI);
            s.setStageDate(parseDate(req.getJiDate(), "JI date", false));
            applyJiTeam(s, c, req);
            transition = TR_CM_SCHEDULE_JI;
            toStatus = ST_JI_SCHEDULED;
        } else if (PATH_AUDIT.equals(path)) {
            s.setPath(PATH_AUDIT);
            transition = TR_CM_START_AUDIT;
            toStatus = ST_AUDIT_IN_PROGRESS;
        } else {
            throw validation("Please choose Joint Inspection or Audit.");
        }

        stageRepository.save(s);
        return move(c, transition, toStatus, remarks, userId, ROLE_CM_NAME, ROLE_CM_ID);
    }

    /** JI team options: all CMs (every RIO) first, then all IEs (ERC / Sleeper / Rail Pad; no Process IE). */
    @Override
    @Transactional(readOnly = true)
    public List<JiTeamOptionDto> getJiTeamOptions(Long udmRejectionId, Integer userId) {
        UdmComplaint c = complaintRepository.findByUdmRejectionId(udmRejectionId)
                .orElseThrow(() -> validation("This complaint has not been sent to a Controlling Manager yet."));
        if (!isAssignedCm(c, userId)) {
            throw validation("Only the Controlling Manager assigned to this complaint can choose the JI team.");
        }
        return jiTeamOptions(c);
    }

    private List<JiTeamOptionDto> jiTeamOptions(UdmComplaint c) {
        Map<Integer, JiTeamOptionDto> byId = new LinkedHashMap<>();
        for (Object[] r : complaintRepository.findAllCms(ROLE_CM_ID)) {
            Integer id = r[0] != null ? ((Number) r[0]).intValue() : null;
            if (id != null) byId.put(id, new JiTeamOptionDto(id, str(r[1]), str(r[2]), str(r[3]), JI_TYPE_CM));
        }
        for (Object[] r : complaintRepository.findInspectingEngineers()) {
            Integer id = r[0] != null ? ((Number) r[0]).intValue() : null;
            if (id != null) byId.putIfAbsent(id, new JiTeamOptionDto(id, str(r[1]), str(r[2]), str(r[3]), JI_TYPE_IE));
        }
        return new ArrayList<>(byId.values());
    }

    /** Validates selected members against the allowed list and stores names + ids (+ optional others). */
    private void applyJiTeam(UdmComplaintStage s, UdmComplaint c, CmDecisionRequest req) {
        List<Integer> ids = req.getJiTeamUserIds() == null ? List.of()
                : req.getJiTeamUserIds().stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) throw validation("Select at least one JI team member.");
        if (ids.size() > JI_TEAM_MAX_MEMBERS) throw validation("JI team can have at most " + JI_TEAM_MAX_MEMBERS + " members.");

        Map<Integer, JiTeamOptionDto> allowed = new HashMap<>();
        jiTeamOptions(c).forEach(o -> allowed.put(o.getUserId(), o));
        List<String> names = new ArrayList<>();
        for (Integer id : ids) {
            JiTeamOptionDto o = allowed.get(id);
            if (o == null) throw validation("Selected JI team member is not an IE or a Controlling Manager.");
            names.add(o.getName() + (isBlank(o.getEmployeeCode()) ? "" : " (" + o.getEmployeeCode() + ")") + " – " + o.getType());
        }
        String others = isBlank(req.getJiTeamOthers()) ? null : req.getJiTeamOthers().trim();
        if (others != null && others.length() > JI_TEAM_OTHERS_MAX) {
            throw validation("Other members must be at most " + JI_TEAM_OTHERS_MAX + " characters.");
        }
        String team = String.join("; ", names) + (others != null ? "; Others: " + others : "");
        if (team.length() > JI_TEAM_MAX) throw validation("JI team is too long. Please select fewer members.");
        s.setJiTeam(team);
        s.setJiTeamUserIds(ids.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse(null));
    }

    private static String str(Object o) { return o == null ? "" : o.toString(); }

    // ------------------------------------------------------------------ stage 3A: complete JI

    @Override
    @Transactional
    public UdmComplaintStatusDto completeJi(Long udmRejectionId, CompleteJiRequest req, MultipartFile report, Integer userId) {
        if (req == null) throw validation("JI details are required.");
        UdmComplaint c = loadForCm(udmRejectionId, userId, STAGE_JI_COMPLETE);
        Map<String, List<String>> dd = getDropdowns();

        UdmComplaintStage s = newStage(c, STAGE_JI_COMPLETE, userId);
        s.setStageDate(parseDate(req.getActualJiDate(), "Actual JI date", true));
        s.setJiOutcome(oneOf(req.getJiOutcome(), dd.get(DD_JI_OUTCOME), "JI outcome"));
        s.setAttributableTo(oneOf(req.getAttributableTo(), dd.get(DD_ATTRIBUTABLE_TO), "Attributable to"));
        s.setRemarks(support.trimRemarks(req.getRemarks(), false));

        ValidPdf pdf = storage.validate(report, true, "JI Report");
        stageRepository.save(s);
        saveDocument(c, STAGE_JI_COMPLETE, DOC_JI_REPORT, pdf, userId);
        return move(c, TR_CM_COMPLETE_JI, ST_JI_COMPLETED, s.getRemarks(), userId, ROLE_CM_NAME, ROLE_CM_ID);
    }

    // ------------------------------------------------------------------ stage 3B: complete audit

    @Override
    @Transactional
    public UdmComplaintStatusDto completeAudit(Long udmRejectionId, CompleteAuditRequest req, MultipartFile report, Integer userId) {
        if (req == null) throw validation("Audit details are required.");
        UdmComplaint c = loadForCm(udmRejectionId, userId, STAGE_AUDIT_COMPLETE);
        Map<String, List<String>> dd = getDropdowns();

        UdmComplaintStage s = newStage(c, STAGE_AUDIT_COMPLETE, userId);
        s.setAuditType(oneOf(req.getAuditType(), dd.get(DD_AUDIT_TYPE), "Audit type"));
        s.setStageDate(parseDate(req.getAuditDate(), "Audit date", true));
        s.setFindings(requiredText(req.getFindings(), "Findings", FINDINGS_MAX));
        s.setAttributableTo(oneOf(req.getAttributableTo(), dd.get(DD_ATTRIBUTABLE_TO), "Attributable to"));
        s.setRemarks(support.trimRemarks(req.getRemarks(), false));

        ValidPdf pdf = storage.validate(report, true, "Audit Report");
        stageRepository.save(s);
        saveDocument(c, STAGE_AUDIT_COMPLETE, DOC_AUDIT_REPORT, pdf, userId);
        return move(c, TR_CM_COMPLETE_AUDIT, ST_AUDIT_COMPLETED, s.getRemarks(), userId, ROLE_CM_NAME, ROLE_CM_ID);
    }

    // ------------------------------------------------------------------ stage 4: RCA

    @Override
    @Transactional
    public UdmComplaintStatusDto submitRca(Long udmRejectionId, RcaRequest req, MultipartFile evidence, Integer userId) {
        if (req == null) throw validation("Root cause is required.");
        UdmComplaint c = loadForCm(udmRejectionId, userId, STAGE_RCA);

        UdmComplaintStage s = newStage(c, STAGE_RCA, userId);
        s.setRootCause(oneOf(req.getRootCause(), getDropdowns().get(DD_ROOT_CAUSE), "Root cause"));
        s.setRemarks(support.trimRemarks(req.getRemarks(), false));

        ValidPdf pdf = storage.validate(evidence, false, "RCA evidence");
        stageRepository.save(s);
        if (pdf != null) saveDocument(c, STAGE_RCA, DOC_RCA_EVIDENCE, pdf, userId);
        return move(c, TR_CM_SUBMIT_RCA, ST_RCA_SUBMITTED, s.getRemarks(), userId, ROLE_CM_NAME, ROLE_CM_ID);
    }

    // ------------------------------------------------------------------ stage 5: CAPA (→ SBU Head)

    @Override
    @Transactional
    public UdmComplaintStatusDto submitCapa(Long udmRejectionId, CapaRequest req, MultipartFile evidence, Integer userId) {
        if (req == null) throw validation("CAPA details are required.");
        UdmComplaint c = loadForCm(udmRejectionId, userId, STAGE_CAPA);
        Map<String, List<String>> dd = getDropdowns();

        UdmComplaintStage s = newStage(c, STAGE_CAPA, userId);
        s.setCorrectiveActions(multi(req.getCorrectiveActions(), dd.get(DD_CORRECTIVE), "Corrective action"));
        s.setPreventiveActions(multi(req.getPreventiveActions(), dd.get(DD_PREVENTIVE), "Preventive action"));
        s.setVendorCompliance(yesNo(req.getVendorCompliance(), "Vendor compliance"));
        s.setCapaImplemented(yesNo(req.getCapaImplemented(), "CAPA implemented"));
        s.setRemarks(support.trimRemarks(req.getRemarks(), false));

        ValidPdf pdf = storage.validate(evidence, false, "CAPA evidence");
        stageRepository.save(s);
        if (pdf != null) saveDocument(c, STAGE_CAPA, DOC_CAPA_EVIDENCE, pdf, userId);
        return move(c, TR_CM_SUBMIT_CAPA, ST_CAPA_SUBMITTED, s.getRemarks(), userId, ROLE_SBU_HEAD_NAME, ROLE_SBU_HEAD_ID);
    }

    // ------------------------------------------------------------------ reads

    @Override
    @Transactional(readOnly = true)
    public List<UdmComplaintStageDto> getStages(Long udmRejectionId) {
        return stageRepository.findByUdmRejectionIdOrderByCreatedAtAsc(udmRejectionId).stream().map(s -> {
            UdmComplaintStageDto d = new UdmComplaintStageDto();
            d.setId(s.getId());
            d.setStage(s.getStage());
            d.setRoundNo(s.getRoundNo());
            d.setPath(s.getPath());
            d.setFlagFailed90(s.getFlagFailed90());
            d.setFlagQtyOver5(s.getFlagQtyOver5());
            d.setFlagCritical(s.getFlagCritical());
            d.setStageDate(s.getStageDate() != null ? s.getStageDate().toString() : null);
            d.setJiTeam(s.getJiTeam());
            d.setJiOutcome(s.getJiOutcome());
            d.setAuditType(s.getAuditType());
            d.setFindings(s.getFindings());
            d.setAttributableTo(s.getAttributableTo());
            d.setRootCause(s.getRootCause());
            d.setCorrectiveActions(split(s.getCorrectiveActions()));
            d.setPreventiveActions(split(s.getPreventiveActions()));
            d.setVendorCompliance(s.getVendorCompliance());
            d.setCapaImplemented(s.getCapaImplemented());
            d.setRemarks(s.getRemarks());
            d.setCreatedBy(s.getCreatedBy());
            d.setCreatedAt(s.getCreatedAt() != null ? s.getCreatedAt().toString() : null);
            return d;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UdmComplaintDocumentDto> getDocuments(Long udmRejectionId) {
        return documentRepository.findByUdmRejectionIdAndActiveTrueOrderByUploadedAtAsc(udmRejectionId).stream().map(x -> {
            UdmComplaintDocumentDto d = new UdmComplaintDocumentDto();
            d.setId(x.getId());
            d.setStage(x.getStage());
            d.setRoundNo(x.getRoundNo());
            d.setDocType(x.getDocType());
            d.setFileName(x.getOriginalName());
            d.setSizeBytes(x.getSizeBytes());
            d.setUploadedBy(x.getUploadedBy());
            d.setUploadedAt(x.getUploadedAt() != null ? x.getUploadedAt().toString() : null);
            return d;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UdmDocumentFile downloadDocument(Long documentId) {
        UdmComplaintDocument doc = documentRepository.findByIdAndActiveTrue(documentId)
                .orElseThrow(() -> validation("Document not found."));
        return new UdmDocumentFile(doc.getOriginalName(), doc.getContentType(), storage.download(doc.getBlobName()));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<String>> getDropdowns() {
        Map<String, List<String>> out = new LinkedHashMap<>();
        for (Object[] row : dropdownRepository.findActiveValues()) {
            if (row[0] == null || row[1] == null) continue;
            out.computeIfAbsent(row[0].toString(), k -> new ArrayList<>()).add(row[1].toString());
        }
        return out;
    }

    // ------------------------------------------------------------------ helpers

    /** Loads the complaint, checks the user is its assigned CM and that this form is the one due now. */
    private UdmComplaint loadForCm(Long udmRejectionId, Integer userId, String stage) {
        if (userId == null) throw validation("User not identified.");
        UdmComplaint c = complaintRepository.findByUdmRejectionId(udmRejectionId)
                .orElseThrow(() -> validation("This complaint has not been sent to a Controlling Manager yet."));
        if (!isAssignedCm(c, userId)) {
            throw validation("Only the Controlling Manager assigned to this complaint can perform this action.");
        }
        if (!stage.equals(NEXT_STAGE.get(c.getStatus()))) {
            throw validation("This action is not allowed at the current status (" + c.getStatus() + "). Please refresh.");
        }
        return c;
    }

    /** Assigned CM who still holds the CM role in the same RIO (permissions from user_role_master). */
    private boolean isAssignedCm(UdmComplaint c, Integer userId) {
        return userId != null && userId.equals(c.getAssignedCmUserId())
                && c.getAssignedRio() != null
                && complaintRepository.countCmInRio(userId, c.getAssignedRio(), ROLE_CM_ID) > 0;
    }

    private UdmComplaintStatusDto move(UdmComplaint c, String transitionName, String toStatus, String remarks,
                                       Integer userId, String toRoleName, int toRoleId) {
        TransitionMaster tr = support.transition(transitionName);
        String fromStatus = c.getStatus();
        c.setStatus(toStatus);
        c.setPendingWithRoleId(toRoleId);
        c.setPendingWithRoleName(toRoleName);
        if (remarks != null) c.setLastRemarks(remarks);
        c.setUpdatedBy(userId);
        c = support.save(c);   // optimistic lock: a second submit of the same form fails cleanly
        support.logTransition(c, tr, fromStatus, toStatus, ROLE_CM_NAME, toRoleName, toRoleId,
                remarks, c.getAssignedRio(), userId, userId);
        log.info("[UDM-COMPLAINT] {} {} -> {} by CM {}", c.getRejectionNumber(), fromStatus, toStatus, userId);
        return support.toStatusDto(c);
    }

    private UdmComplaintStage newStage(UdmComplaint c, String stage, Integer userId) {
        UdmComplaintStage s = new UdmComplaintStage();
        s.setComplaintId(c.getId());
        s.setUdmRejectionId(c.getUdmRejectionId());
        s.setStage(stage);
        s.setRoundNo(FIRST_ROUND);
        s.setCreatedBy(userId);
        return s;
    }

    private void saveDocument(UdmComplaint c, String stage, String docType, ValidPdf pdf, Integer userId) {
        String blobName = storage.upload(pdf, c.getUdmRejectionId(), stage);
        UdmComplaintDocument d = new UdmComplaintDocument();
        d.setComplaintId(c.getId());
        d.setUdmRejectionId(c.getUdmRejectionId());
        d.setStage(stage);
        d.setRoundNo(FIRST_ROUND);
        d.setDocType(docType);
        d.setOriginalName(pdf.originalName());
        d.setBlobName(blobName);
        d.setContentType("application/pdf");
        d.setSizeBytes((long) pdf.bytes().length);
        d.setUploadedBy(userId);
        documentRepository.save(d);
    }

    /** Days from material receipt (or IC date) to rejection; null when dates are missing. */
    private static Long daysToFailure(UdmRejection r) {
        LocalDateTime base = r.getMaterialReceivedDate() != null ? r.getMaterialReceivedDate() : r.getIcDate();
        if (base == null || r.getRejectionDate() == null) return null;
        return ChronoUnit.DAYS.between(base.toLocalDate(), r.getRejectionDate().toLocalDate());
    }

    private static LocalDate parseDate(String value, String label, boolean notInFuture) {
        if (isBlank(value)) throw validation(label + " is required.");
        LocalDate d;
        try {
            d = LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw validation(label + " is not a valid date.");
        }
        if (notInFuture && d.isAfter(LocalDate.now(IST))) throw validation(label + " cannot be in the future.");
        return d;
    }

    private static String requiredText(String value, String label, int max) {
        if (isBlank(value)) throw validation(label + " is required.");
        String v = value.trim();
        if (v.length() > max) throw validation(label + " must be at most " + max + " characters.");
        return v;
    }

    private static String oneOf(String value, List<String> allowed, String label) {
        if (isBlank(value)) throw validation(label + " is required.");
        String v = value.trim();
        if (allowed == null || allowed.stream().noneMatch(a -> a.equalsIgnoreCase(v))) {
            throw validation("Invalid " + label.toLowerCase(Locale.ROOT) + ": " + v);
        }
        return allowed.stream().filter(a -> a.equalsIgnoreCase(v)).findFirst().orElse(v);
    }

    private static String multi(List<String> values, List<String> allowed, String label) {
        List<String> clean = values == null ? List.of() : values.stream()
                .filter(v -> !isBlank(v)).map(String::trim).distinct().toList();
        if (clean.isEmpty()) throw validation("Select at least one " + label.toLowerCase(Locale.ROOT) + ".");
        List<String> canonical = new ArrayList<>();
        for (String v : clean) canonical.add(oneOf(v, allowed, label));
        if (canonical.size() > 1 && canonical.stream().anyMatch("Nil"::equalsIgnoreCase)) {
            throw validation("\"Nil\" cannot be combined with other " + label.toLowerCase(Locale.ROOT) + "s.");
        }
        return String.join(LIST_SEP, canonical);
    }

    private static String yesNo(String value, String label) {
        String v = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!"Y".equals(v) && !"N".equals(v)) throw validation(label + " must be Yes or No.");
        return v;
    }

    private static List<String> split(String joined) {
        if (isBlank(joined)) return List.of();
        return Arrays.stream(joined.split("\\" + LIST_SEP)).filter(x -> !x.isBlank()).toList();
    }
}
