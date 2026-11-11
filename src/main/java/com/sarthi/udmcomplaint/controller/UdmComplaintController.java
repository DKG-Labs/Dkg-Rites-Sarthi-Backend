package com.sarthi.udmcomplaint.controller;

import com.sarthi.constant.AppConstant;
import com.sarthi.entity.UserMaster;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.udmcomplaint.dto.*;
import com.sarthi.udmcomplaint.service.UdmComplaintCmService;
import com.sarthi.udmcomplaint.service.UdmComplaintWorkflowService;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * UDM complaint workflow (Complaint Cell → Controlling Manager).
 * The acting user always comes from the authenticated principal (JWT), never from request params.
 */
@RestController
@RequestMapping("/api/udm-complaints")
@RequiredArgsConstructor
public class UdmComplaintController {

    private final UdmComplaintWorkflowService service;
    private final UdmComplaintCmService cmService;

    /** RIOs that have Controlling Managers (RIO dropdown). */
    @GetMapping("/rios")
    public ResponseEntity<Object> rios() {
        return ok(service.getRios());
    }

    /** Controlling Managers of a RIO (CM dropdown). */
    @GetMapping("/cms")
    public ResponseEntity<Object> cms(@RequestParam String rio) {
        return ok(service.getCmsByRio(rio));
    }

    /** Complaint Cell: send to Controlling Manager (transaction CC_SEND_TO_CM). */
    @PostMapping("/{rejectionId}/send-to-cm")
    public ResponseEntity<Object> sendToCm(@PathVariable Long rejectionId,
                                           @RequestBody SendToCmRequest request,
                                           @AuthenticationPrincipal Object principal) {
        return ok(service.sendToCm(rejectionId, request, userId(principal)));
    }

    /** Complaint Cell: close (transaction CC_CLOSE_COMPLAINT). */
    @PostMapping("/{rejectionId}/close")
    public ResponseEntity<Object> close(@PathVariable Long rejectionId,
                                        @RequestBody CloseComplaintRequest request,
                                        @AuthenticationPrincipal Object principal) {
        return ok(service.close(rejectionId, request, userId(principal)));
    }

    /** Controlling Manager: complaints assigned to me (paginated). */
    @GetMapping("/my-assigned")
    public ResponseEntity<Object> myAssigned(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "50") int size,
                                             @AuthenticationPrincipal Object principal) {
        return ok(service.getAssignedToCm(userId(principal), page, size));
    }

    /** Timeline of a complaint. */
    @GetMapping("/{rejectionId}/history")
    public ResponseEntity<Object> history(@PathVariable Long rejectionId) {
        return ok(service.getHistory(rejectionId));
    }

    // ================================================================ Phase 2 — Controlling Manager stages

    /** CM form context: status, can-act, next form, 90-day flag, dropdown values. */
    @GetMapping("/{rejectionId}/cm-context")
    public ResponseEntity<Object> cmContext(@PathVariable Long rejectionId, @AuthenticationPrincipal Object principal) {
        return ok(cmService.getCmContext(rejectionId, userId(principal)));
    }

    /** JI team options: CMs of the RIO + all IEs (ERC / Sleeper / Rail Pad; Process IE excluded). Assigned CM only. */
    @GetMapping("/{rejectionId}/cm/ji-team-options")
    public ResponseEntity<Object> jiTeamOptions(@PathVariable Long rejectionId, @AuthenticationPrincipal Object principal) {
        return ok(cmService.getJiTeamOptions(rejectionId, userId(principal)));
    }

    /** CM Decision: Joint Inspection (with schedule) or Audit (JSON). */
    @PostMapping("/{rejectionId}/cm/decision")
    public ResponseEntity<Object> decision(@PathVariable Long rejectionId,
                                           @RequestBody CmDecisionRequest request,
                                           @AuthenticationPrincipal Object principal) {
        return ok(cmService.decide(rejectionId, request, userId(principal)));
    }

    /** Complete JI: multipart "data" (JSON) + "file" (JI Report PDF, required, max 5 MB). */
    @PostMapping(value = "/{rejectionId}/cm/complete-ji", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> completeJi(@PathVariable Long rejectionId,
                                             @RequestPart("data") CompleteJiRequest request,
                                             @RequestPart(value = "file", required = false) MultipartFile file,
                                             @AuthenticationPrincipal Object principal) {
        return ok(cmService.completeJi(rejectionId, request, file, userId(principal)));
    }

    /** Complete Audit: multipart "data" (JSON) + "file" (Audit Report PDF, required, max 5 MB). */
    @PostMapping(value = "/{rejectionId}/cm/complete-audit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> completeAudit(@PathVariable Long rejectionId,
                                                @RequestPart("data") CompleteAuditRequest request,
                                                @RequestPart(value = "file", required = false) MultipartFile file,
                                                @AuthenticationPrincipal Object principal) {
        return ok(cmService.completeAudit(rejectionId, request, file, userId(principal)));
    }

    /** RCA: multipart "data" (JSON) + optional "file" (evidence PDF, max 5 MB). */
    @PostMapping(value = "/{rejectionId}/cm/rca", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> rca(@PathVariable Long rejectionId,
                                      @RequestPart("data") RcaRequest request,
                                      @RequestPart(value = "file", required = false) MultipartFile file,
                                      @AuthenticationPrincipal Object principal) {
        return ok(cmService.submitRca(rejectionId, request, file, userId(principal)));
    }

    /** CAPA: multipart "data" (JSON) + optional "file" (evidence PDF, max 5 MB). Moves to SBU Head. */
    @PostMapping(value = "/{rejectionId}/cm/capa", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> capa(@PathVariable Long rejectionId,
                                       @RequestPart("data") CapaRequest request,
                                       @RequestPart(value = "file", required = false) MultipartFile file,
                                       @AuthenticationPrincipal Object principal) {
        return ok(cmService.submitCapa(rejectionId, request, file, userId(principal)));
    }

    /** Submitted stage details (for the timeline / View). */
    @GetMapping("/{rejectionId}/stages")
    public ResponseEntity<Object> stages(@PathVariable Long rejectionId) {
        return ok(cmService.getStages(rejectionId));
    }

    /** Documents of a complaint. */
    @GetMapping("/{rejectionId}/documents")
    public ResponseEntity<Object> documents(@PathVariable Long rejectionId) {
        return ok(cmService.getDocuments(rejectionId));
    }

    /** Stream a document (container stays private; no public blob URLs). */
    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long documentId) {
        UdmDocumentFile f = cmService.downloadDocument(documentId);
        String safeName = f.fileName().replaceAll("[\\r\\n\"]", "_");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + safeName + "\"")
                .body(f.content());
    }

    /** SRS dropdown values grouped by category. */
    @GetMapping("/dropdowns")
    public ResponseEntity<Object> dropdowns() {
        return ok(cmService.getDropdowns());
    }

    private static ResponseEntity<Object> ok(Object body) {
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(body), HttpStatus.OK);
    }

    private static Integer userId(Object principal) {
        if (principal instanceof UserMaster u && u.getUserId() != null) return u.getUserId();
        throw new BusinessException(new ErrorDetails(
                AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE,
                AppConstant.ERROR_TYPE_VALIDATION, "User not identified. Please log in again."));
    }
}
