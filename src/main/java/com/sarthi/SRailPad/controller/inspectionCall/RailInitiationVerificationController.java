package com.sarthi.SRailPad.controller.inspectionCall;

import com.sarthi.SRailPad.dto.RailInitiationVerificationDto;
import com.sarthi.SRailPad.entity.inspectionCall.RailInitiationVerification;
import com.sarthi.SRailPad.service.inspectionCall.RailInitiationVerificationService;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST API for storing Section A & B verification data
 * when the IE officer clicks "OPEN & VERIFY FORM" and submits the shift popup.
 *
 * Completely isolated from the shared Sleeper portal endpoints.
 */
@RestController
@RequestMapping("/api/rail-initiation-verification")
@CrossOrigin(origins = "*")
public class RailInitiationVerificationController {

    @Autowired
    private RailInitiationVerificationService service;

    /**
     * POST /api/rail-initiation-verification/submit
     *
     * Saves (or updates) the Section A & B verification record for a call.
     * Called from the frontend when the IE officer submits the ShiftDutyForm
     * after clicking "OPEN & VERIFY FORM".
     *
     * The frontend should also separately call PO_VERIFICATION workflow transition.
     */
    @PostMapping("/submit")
    public ResponseEntity<Object> submit(@RequestBody RailInitiationVerificationDto dto) {
        if (dto.getCallNo() == null || dto.getCallNo().isBlank()) {
            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(new ErrorDetails(400, 1, "VALIDATION_ERROR", "callNo is required")),
                    HttpStatus.BAD_REQUEST
            );
        }

        try {
            RailInitiationVerification saved = service.save(dto);
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(saved),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(new ErrorDetails(500, 2, "SAVE_ERROR", e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * GET /api/rail-initiation-verification/{callNo}
     *
     * Fetches the saved verification record for a given call number.
     * Useful for pre-populating the UI if the officer revisits the screen.
     */
    @GetMapping("/{callNo}")
    public ResponseEntity<Object> getByCallNo(@PathVariable String callNo) {
        Optional<RailInitiationVerification> record = service.getByCallNo(callNo);

        if (record.isEmpty()) {
            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(new ErrorDetails(404, 1, "NOT_FOUND",
                            "No verification record found for call: " + callNo)),
                    HttpStatus.NOT_FOUND
            );
        }

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(record.get()),
                HttpStatus.OK
        );
    }
}
