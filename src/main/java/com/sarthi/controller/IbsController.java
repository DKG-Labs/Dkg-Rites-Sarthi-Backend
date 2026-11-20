package com.sarthi.controller;


import com.sarthi.dto.IBS.IbsAcknowledgementDto;
import com.sarthi.dto.ibsDtos.AuthRequestDto;
import com.sarthi.dto.ibsDtos.AuthResponseDto;
import com.sarthi.entity.certificate.CertificateStorage;
import com.sarthi.repository.certificate.CertificateStorageRepository;
import com.sarthi.service.AzureBlobStorageService;
import com.sarthi.service.IbsService;

import com.sarthi.service.JwtService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class IbsController {

    @Autowired
    private IbsService service;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private com.sarthi.service.Impl.IbsCompletedCallsQueryService completedCallsQuery;


    @PostMapping("/sarthi/authenticate")
    public ResponseEntity<AuthResponseDto> generateIntegrationToken(
            @RequestBody AuthRequestDto request) {

        return ResponseEntity.ok(
                service.integrationLogin(request)
        );
    }

    @GetMapping("/ibs/call-registration-inspection-data")
    public ResponseEntity<Object> getCallData(
            @RequestHeader(value = "Authorization",
                    required = false) String authHeader,
            @RequestParam(value = "status", required = false) String status) {

      jwtService.validateToken(authHeader);

        if ("COMPLETED".equalsIgnoreCase(status)) {
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(
                            service.getCompletedIbsCalls()
                    ),
                    HttpStatus.OK
            );
        }

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        service.getAllGeneratedIcCalls()
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/ibs/completed-inspection-data")
    public ResponseEntity<Object> getCompletedCallData(
            @RequestHeader(value = "Authorization",
                    required = false) String authHeader) {

        jwtService.validateToken(authHeader);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        service.getCompletedIbsCalls()
                ),
                HttpStatus.OK
        );
    }

    @PostMapping("/ibs/call-data/acknowledgement")
    public ResponseEntity<Object> acknowledgeCallData(
            @RequestHeader(value = "Authorization",
                    required = false) String authHeader,  @RequestBody IbsAcknowledgementDto dto
    ) {

        jwtService.validateToken(authHeader);
        Object ackResult = service.acknowledgeCallData(dto);
        completedCallsQuery.invalidate();
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(
                        ackResult
                ),
                HttpStatus.OK
        );
    }

    @PostMapping("/ibs/get-case-no")
    public ResponseEntity<Object> getIbsCaseNo(@RequestBody Map<String, Object> payload) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getIbsCaseNo(payload)),
                HttpStatus.OK
        );
    }

    @PostMapping("/ibs/save-case-no")
    public ResponseEntity<Object> saveIbsCaseNo(@RequestBody Map<String, Object> payload) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(saveCaseNoAndInvalidate(payload)),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/ibs/revert-to-completed")
    public ResponseEntity<Object> revertIbsCallToCompleted(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam("callNumber") String callNumber,
            @RequestParam(value = "deletedBy", required = false) Integer deletedBy) {

        jwtService.validateToken(authHeader);
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        revertAndInvalidate(callNumber, deletedBy)
                ),
                HttpStatus.OK
        );
    }

    private Object saveCaseNoAndInvalidate(Map<String, Object> payload) {
        Object r = service.saveIbsCaseNo(payload);
        completedCallsQuery.invalidate();
        return r;
    }

    private Object revertAndInvalidate(String callNumber, Integer deletedBy) {
        Object r = service.revertIbsCallToCompleted(callNumber, deletedBy);
        completedCallsQuery.invalidate();
        return r;
    }

    /**
     * Completed calls, filtered / sorted / paged on the server.
     * size=0 returns every matching row (CSV export). refresh=true rebuilds the cached list.
     */
    @GetMapping("/ibs/completed-inspection-data/search")
    public ResponseEntity<Object> searchCompletedCallData(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String product,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "ACK") String dateField,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String sortKey,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "false") boolean refresh) {

        jwtService.validateToken(authHeader);
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(completedCallsQuery.search(
                        page, Math.min(size, 500), category, product, region, status, search,
                        dateField, from, to, sortKey, sortDir, refresh)),
                HttpStatus.OK);
    }
}
