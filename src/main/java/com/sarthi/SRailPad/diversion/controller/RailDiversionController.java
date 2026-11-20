package com.sarthi.SRailPad.diversion.controller;

import com.sarthi.SRailPad.diversion.dto.RailDiversionDtos.ActionDto;
import com.sarthi.SRailPad.diversion.dto.RailDiversionDtos.RequestDto;
import com.sarthi.SRailPad.diversion.entity.RailDiversionRequest;
import com.sarthi.SRailPad.diversion.service.RailDiversionService;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.exception.InvalidInputException;
import com.sarthi.service.AzureBlobStorageService;
import com.sarthi.util.APIResponse;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

/**
 * Rail Pad material diversion (Process / Final). Mirrors /api/erc/diversion.
 * Vendor, CM and SBU identity always come from the login token.
 */
@RestController
@RequestMapping("/api/railpad/diversion")
@RequiredArgsConstructor
public class RailDiversionController {

    private static final String DOC_CONTAINER = "sarthi-diversion-docs";
    private static final long MAX_DOC_BYTES = 5L * 1024 * 1024;

    private final RailDiversionService service;
    private final AzureBlobStorageService storage;

    private static ResponseEntity<APIResponse> ok(Object o) {
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(o));
    }

    // ---------- Vendor ----------

    @GetMapping("/eligible-source-ics")
    public ResponseEntity<APIResponse> eligibleSourceIcs(@RequestParam(required = false) String plantId, @RequestParam String stage) {
        return ok(service.getEligibleSourceIcs(plantId, stage));
    }

    @GetMapping("/source-ic-details")
    public ResponseEntity<APIResponse> sourceIcDetails(@RequestParam String icNo, @RequestParam String stage,
                                                       @RequestParam(required = false) Long excludeRequestId) {
        return ok(service.getSourceLines(icNo, stage, excludeRequestId));
    }

    @PostMapping("/upload-railway-doc")
    public ResponseEntity<APIResponse> uploadRailwayDoc(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) throw invalid("Choose the railway permission letter (PDF)");
        String name = file.getOriginalFilename() == null ? "railway_doc.pdf" : file.getOriginalFilename();
        if (!name.toLowerCase().endsWith(".pdf")) throw invalid("Railway permission letter must be a PDF");
        if (file.getSize() > MAX_DOC_BYTES) throw invalid("Railway permission letter must be 5 MB or smaller");
        String blobName = "railpad/railway/" + UUID.randomUUID() + "_" + name.replaceAll("[^a-zA-Z0-9._\\-]", "_");
        String url = storage.uploadFileBytes(file.getBytes(), blobName, DOC_CONTAINER);
        return ok(Map.of("url", url, "blobName", blobName));
    }

    /** Streams the railway permission letter (container is private). Access-checked via getById. */
    @GetMapping("/doc/{id}")
    public ResponseEntity<byte[]> doc(@PathVariable Long id) {
        RailDiversionRequest req = service.getById(id);
        String url = req.getRailwayPermissionDocUrl();
        if (url == null || url.isBlank()) return ResponseEntity.notFound().build();
        byte[] bytes;
        if (url.startsWith("data:")) {
            bytes = java.util.Base64.getDecoder().decode(url.substring(url.indexOf(',') + 1).trim());
        } else {
            int i = url.indexOf(DOC_CONTAINER + "/");
            String blob = i >= 0 ? url.substring(i + DOC_CONTAINER.length() + 1) : url;
            bytes = storage.downloadFileFromContainer(java.net.URLDecoder.decode(blob, java.nio.charset.StandardCharsets.UTF_8), DOC_CONTAINER);
        }
        if (bytes == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"railway-permission-" + req.getRequestNo() + ".pdf\"")
                .body(bytes);
    }

    @PostMapping("/submit")
    public ResponseEntity<APIResponse> submit(@RequestBody RequestDto dto) {
        return ok(service.submit(dto));
    }

    @PutMapping("/request/{id}")
    public ResponseEntity<APIResponse> update(@PathVariable Long id, @RequestBody RequestDto dto) {
        return ok(service.update(id, dto));
    }

    @PostMapping("/request/{id}/resubmit")
    public ResponseEntity<APIResponse> resubmit(@PathVariable Long id) {
        return ok(service.resubmit(id));
    }

    @DeleteMapping("/request/{id}")
    public ResponseEntity<APIResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ok(Map.of("deleted", id));
    }

    @GetMapping("/request/{id}")
    public ResponseEntity<APIResponse> get(@PathVariable Long id) {
        return ok(service.getById(id));
    }

    @GetMapping("/vendor/requests")
    public ResponseEntity<APIResponse> vendorRequests() {
        return ok(service.getVendorRequests());
    }

    @GetMapping("/basket/vendor")
    public ResponseEntity<APIResponse> vendorBasket() {
        return ok(service.getVendorBasket());
    }

    @GetMapping("/complete-source-sr-nos")
    public ResponseEntity<APIResponse> completeSourceSrNos() {
        return ok(service.getCompleteSourceSrNos());
    }

    /** Diverted ICs on a target PO serial with remaining balance (for raising a DIVERTED call). */
    @GetMapping("/diverted-ics")
    public ResponseEntity<APIResponse> divertedIcs(@RequestParam String poNo, @RequestParam String poSrNo, @RequestParam String stage) {
        return ok(service.getDivertedIcBalances(poNo, poSrNo, stage));
    }

    @GetMapping("/history/{id}")
    public ResponseEntity<APIResponse> history(@PathVariable Long id) {
        return ok(service.getHistory(id));
    }

    // ---------- CM / SBU ----------

    @GetMapping("/cm/pending-requests")
    public ResponseEntity<APIResponse> cmPending() {
        return ok(service.getCmPending());
    }

    @GetMapping("/cm/approved-requests")
    public ResponseEntity<APIResponse> cmActioned() {
        return ok(service.getCmActioned());
    }

    @GetMapping("/sbu/pending-requests")
    public ResponseEntity<APIResponse> sbuPending() {
        return ok(service.getSbuPending());
    }

    @GetMapping("/sbu/approved-requests")
    public ResponseEntity<APIResponse> sbuActioned() {
        return ok(service.getSbuActioned());
    }

    @PostMapping("/cm/approve")
    public ResponseEntity<APIResponse> cmApprove(@RequestBody ActionDto a) {
        return ok(service.cmApprove(a));
    }

    @PostMapping("/cm/send-to-sbu")
    public ResponseEntity<APIResponse> cmSendToSbu(@RequestBody ActionDto a) {
        return ok(service.cmSendToSbu(a));
    }

    @PostMapping("/cm/return")
    public ResponseEntity<APIResponse> cmReturn(@RequestBody ActionDto a) {
        return ok(service.cmReturn(a));
    }

    @PostMapping("/sbu/approve")
    public ResponseEntity<APIResponse> sbuApprove(@RequestBody ActionDto a) {
        return ok(service.sbuApprove(a));
    }

    @PostMapping("/sbu/return")
    public ResponseEntity<APIResponse> sbuReturn(@RequestBody ActionDto a) {
        return ok(service.sbuReturn(a));
    }

    private static InvalidInputException invalid(String msg) {
        return new InvalidInputException(new ErrorDetails(AppConstant.INVALID_INPUT,
                AppConstant.ERROR_TYPE_CODE_VALIDATION, AppConstant.ERROR_TYPE_VALIDATION, msg));
    }
}
