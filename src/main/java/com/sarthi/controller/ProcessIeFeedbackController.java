package com.sarthi.controller;

import com.sarthi.dto.FeedbackTransitionActionReqDto;
import com.sarthi.dto.FeedbackWorkflowTransitionDto;
import com.sarthi.dto.PendingFeedbackRequestDto;
import com.sarthi.service.FeedbackWorkflowService;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback-workflow")
@RequiredArgsConstructor
public class ProcessIeFeedbackController {

    private final FeedbackWorkflowService feedbackWorkflowService;
    private final com.sarthi.service.ProcessInspectionDiscrepancyService processInspectionDiscrepancyService;

        @PostMapping("/initiateWorkflow")
        public ResponseEntity<Object> initiateFeedbackWorkflow(
                @RequestParam String feedbackId,
                @RequestParam Integer createdBy,
                @RequestParam String productType,
                @RequestParam(required = false) String poiCode,
                @RequestParam(required = false) String plantId,
                @RequestParam Integer roleId) {

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(
                            feedbackWorkflowService.initiateFeedbackWorkflow(
                                    feedbackId,
                                    createdBy,
                                    productType,
                                    poiCode,
                                    plantId,
                                    roleId
                            )
                    ),
                    HttpStatus.OK
            );
        }

        @PostMapping("/performTransitionAction")
        public ResponseEntity<Object> performTransitionAction(
                @RequestBody FeedbackTransitionActionReqDto reqDto) {

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(
                            feedbackWorkflowService.feedbackPerformTransition(reqDto)
                    ),
                    HttpStatus.OK
            );
        }


    @GetMapping("/pending")
    public ResponseEntity<Object> getPendingFeedbacks(
            @RequestParam Integer roleId,
            @RequestParam String productType) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        feedbackWorkflowService.getPendingFeedbacks(
                                roleId,
                                productType)),
                HttpStatus.OK);
    }

    @GetMapping("/feedback-status")
    public ResponseEntity<Object> getFeedbackStatus(
            PendingFeedbackRequestDto request) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        feedbackWorkflowService.getFeedbackStatus(request)),
                HttpStatus.OK);
    }

    @GetMapping("/feedbacks/all-pending")
    public  ResponseEntity<Object> getAllPendingFeedbacks(
            @RequestParam String productType) {


        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        feedbackWorkflowService.getPendingFeedbacks(productType)),
                HttpStatus.OK);
    }

    @GetMapping("/feedbacks/completed")
    public  ResponseEntity<Object> getCompletedFeedbacks(
            @RequestParam String productType) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        feedbackWorkflowService.getCompletedFeedbacks(productType)),
                HttpStatus.OK);

   }

    @GetMapping("/vendors")
    public ResponseEntity<Object> getVendorsByProduct(@RequestParam String productType) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        processInspectionDiscrepancyService.getVendorsByProduct(productType)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/plants")
    public ResponseEntity<Object> getPlantsByVendor(@RequestParam String vendorCode) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        processInspectionDiscrepancyService.getPlantsByVendor(vendorCode)
                ),
                HttpStatus.OK
        );
    }

    @PostMapping(value = "/create-discrepancy", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> createDiscrepancy(
            @RequestPart("discrepancy") com.sarthi.entity.ProcessInspectionDiscrepancy discrepancy,
            @RequestParam String poiCode,
            @RequestPart(value = "file", required = false) org.springframework.web.multipart.MultipartFile file) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        processInspectionDiscrepancyService.createDiscrepancy(discrepancy, poiCode, file)
                ),
                HttpStatus.OK
        );
    }

    @PutMapping("/update-discrepancy/{id}")
    public ResponseEntity<Object> updateDiscrepancy(
            @PathVariable Long id,
            @RequestBody com.sarthi.entity.ProcessInspectionDiscrepancy discrepancy,
            @RequestParam Integer actionBy) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        processInspectionDiscrepancyService.updateDiscrepancy(id, discrepancy, actionBy)
                ),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/delete-discrepancy/{id}")
    public ResponseEntity<Object> deleteDiscrepancy(
            @PathVariable Long id,
            @RequestParam Integer actionBy) {
        processInspectionDiscrepancyService.deleteDiscrepancy(id, actionBy);
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse("Discrepancy deleted successfully"),
                HttpStatus.OK
        );
    }

    @PostMapping("/vendor-rectification/{discrepancyNo}")
    public ResponseEntity<Object> vendorRectification(
            @PathVariable String discrepancyNo,
            @RequestBody com.sarthi.entity.ProcessInspectionDiscrepancy rectificationDetails,
            @RequestParam Integer actionBy) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        processInspectionDiscrepancyService.vendorRectification(discrepancyNo, rectificationDetails, actionBy)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/completed")
    public ResponseEntity<Object> getCompletedDiscrepancies(
            @RequestParam Integer roleId,
            @RequestParam String productType,
            @RequestParam Integer userId) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        processInspectionDiscrepancyService.getCompletedDiscrepancies(roleId, productType, userId)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/download-document/{discrepancyNo}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable String discrepancyNo) {
        try {
            byte[] fileBytes = processInspectionDiscrepancyService.getDecompressedDocument(discrepancyNo);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
            // We can't know the exact original filename extension here easily, so we serve it as a generic attachment
            headers.setContentDispositionFormData("attachment", discrepancyNo + "_document");
            
            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
