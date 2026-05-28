package com.sarthi.controller;

import com.sarthi.dto.CallLetterDetailsDto;
import com.sarthi.service.CallLetterService;
import com.sarthi.util.ResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller that exposes the Call Letter Details endpoint.
 * Used by the Call Desk frontend to fetch enriched data for PDF generation.
 *
 * GET /api/call-letter/details?requestId=ER-03280001
 */
@RestController
@RequestMapping("/api/call-letter")
@CrossOrigin(origins = "*")
public class CallLetterController {

    private static final Logger logger = LoggerFactory.getLogger(CallLetterController.class);

    @Autowired
    private CallLetterService callLetterService;

    /**
     * Returns all data needed to render the Call Letter PDF.
     *
     * @param requestId the IC number (e.g. "ER-03280001")
     * @return enriched CallLetterDetailsDto
     */
    @GetMapping("/details")
    public ResponseEntity<Object> getCallLetterDetails(@RequestParam String requestId) {
        logger.info("Call Letter details requested for requestId: {}", requestId);
        try {
            CallLetterDetailsDto details = callLetterService.getCallLetterDetails(requestId);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(details), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching call letter details for requestId: {}", requestId, e);
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(null),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
