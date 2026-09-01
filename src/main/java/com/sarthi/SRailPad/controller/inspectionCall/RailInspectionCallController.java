package com.sarthi.SRailPad.controller.inspectionCall;

import com.sarthi.SRailPad.dto.RailPoSummaryDto;
import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall;
import com.sarthi.SRailPad.repository.RailWorkflowTransactionRepository;
import com.sarthi.SRailPad.service.inspectionCall.RailInspectionCallService;
import com.sarthi.SRailPad.service.RailWorkflowService;
import com.sarthi.SRailPad.service.inspectionCall.RailPoSummaryService;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rail-inspection-call")
@CrossOrigin(origins = "*")
public class RailInspectionCallController {

    private final RailInspectionCallService service;
    private final RailWorkflowService railWorkflowService;

    /**
     * Railpad-specific PO summary service.
     * Does NOT use the shared Sleeper PoDataService — completely isolated.
     */
    private final RailPoSummaryService railPoSummaryService;
    private final com.sarthi.SRailPad.repository.inspectionCall.RailProcessCallDetailsRepository processCallDetailsRepository;
    private final RailWorkflowTransactionRepository railWorkflowTransactionRepository;
    private final com.sarthi.SRailPad.repository.inspectionCall.RailInspectionCallRepository railInspectionCallRepository;

    @Autowired
    public RailInspectionCallController(RailInspectionCallService service,
                                        RailWorkflowService railWorkflowService,
                                        RailPoSummaryService railPoSummaryService,
                                        com.sarthi.SRailPad.repository.inspectionCall.RailProcessCallDetailsRepository processCallDetailsRepository,
                                        RailWorkflowTransactionRepository railWorkflowTransactionRepository,
                                        com.sarthi.SRailPad.repository.inspectionCall.RailInspectionCallRepository railInspectionCallRepository) {
        this.service = service;
        this.railWorkflowService = railWorkflowService;
        this.railPoSummaryService = railPoSummaryService;
        this.processCallDetailsRepository = processCallDetailsRepository;
        this.railWorkflowTransactionRepository = railWorkflowTransactionRepository;
        this.railInspectionCallRepository = railInspectionCallRepository;
    }

    /**
     * GET /api/rail-inspection-call/summary/{callNo}
     *
     * Returns a RailPoSummaryDto containing:
     *   - Section A: po_header fields (rly + po_no, po_date, vendor, purchasing authority, BPO)
     *   - Section B: po_item fields matched by po_sr (qty, uom, consignee, delivery dates)
     *   - Section C: rail_inspection_call fields (erc_type, total_qty, place of inspection)
     *
     * Uses the Railpad-isolated RailPoSummaryService — no effect on the Sleeper portal.
     */
    @GetMapping("/summary/{callNo}")
    public ResponseEntity<Object> getSummary(@PathVariable String callNo) {
        RailInspectionCall call = service.getByCallNo(callNo);
        if (call == null) {
            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(new ErrorDetails(404, 1, "NOT_FOUND", "Call not found")),
                    HttpStatus.NOT_FOUND
            );
        }

        // Use the Railpad-specific service which correctly resolves rlyPoNo and item-specific qty
        RailPoSummaryDto summary = railPoSummaryService.getSummaryByPoAndSr(call.getPoNo(), call.getPoSr());

        if (summary == null) {
            // Graceful fallback when PO header is not yet synced from CRIS
            summary = new RailPoSummaryDto();
            summary.setPoNo(call.getPoNo());
            summary.setRlyPoNo("N/A");
            summary.setPoDate("N/A");
            summary.setVendorCode(call.getVendorCode());
            summary.setVendorName("N/A");
            summary.setItemDesc("Rail Pad");
            summary.setUnit("Nos.");
            summary.setPoSrQty(call.getTotalQty() != null ? call.getTotalQty() : 0);
            summary.setPlaceOfInspection(call.getPlantId());
            summary.setMaNo("N/A");
            summary.setMaDate("N/A");
            summary.setCaseNo("N/A");
        }

        // Always overlay Railpad-specific fields from rail_inspection_call
        summary.setErcType(call.getRailPadType());
        summary.setTotalOfferedQty(call.getTotalQty());
        summary.setRemarks(call.getRemarks());
        String effectiveCallSr = (call.getPoSr() != null && !call.getPoSr().isBlank() && !"null".equalsIgnoreCase(call.getPoSr())) ? call.getPoSr().trim() : null;
        if (effectiveCallSr == null && call.getPoNo() != null && call.getPoNo().contains("/")) {
            String[] parts = call.getPoNo().split("/");
            effectiveCallSr = parts[parts.length - 1].trim();
        }
        if (effectiveCallSr != null && !effectiveCallSr.isBlank()) {
            summary.setPoSerialNo(effectiveCallSr);
            if (summary.getRlyPoNo() != null) {
                summary.setRlyPoNoSerial(summary.getRlyPoNo() + "/" + effectiveCallSr);
            }
        }

        // If it's a PROCESS call, overlay the drawing number from details
        if ("PROCESS".equalsIgnoreCase(call.getCallType())) {
            com.sarthi.SRailPad.entity.inspectionCall.RailProcessCallDetails details = 
                processCallDetailsRepository.findByInspectionCall_CallNo(callNo).orElse(null);
            if (details != null) {
                summary.setDrawingNo(details.getDrawingNo());
                if (details.getDrawingNo() != null) {
                    summary.setItemDesc("Drawing: " + details.getDrawingNo() + (summary.getItemDesc() != null ? " (" + summary.getItemDesc() + ")" : ""));
                }
            }
        }

        // Set rio from the workflow transaction (fetches earliest non-null rio value — set at CREATED stage)
        String rioValue = railWorkflowTransactionRepository.findRioByRequestId(callNo);
        if (rioValue != null && !rioValue.isBlank()) {
            summary.setRio(rioValue);
        }

        // Ensure placeOfInspection fallback to call plantId if null or empty
        if (summary.getPlaceOfInspection() == null || summary.getPlaceOfInspection().isBlank() || "N/A".equalsIgnoreCase(summary.getPlaceOfInspection())) {
            summary.setPlaceOfInspection(call.getPlantId());
        }

        // Calculate Offered Installment Number (count of inspection calls for this PO)
        if (call.getPoNo() != null && !call.getPoNo().isBlank()) {
            long count = railInspectionCallRepository.countByPoNo(call.getPoNo());
            summary.setOfferedInstallmentNo(String.valueOf(count > 0 ? count : 1));
        } else {
            summary.setOfferedInstallmentNo("1");
        }

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(summary),
                HttpStatus.OK
        );
    }

    @PostMapping(value = "/create", consumes = {"application/json", "application/json;charset=UTF-8", "*/*"})
    public ResponseEntity<Object> create(@RequestBody RailInspectionCall call) {
        RailInspectionCall createdCall = service.create(call);
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(createdCall.getCallNo()),
                HttpStatus.OK
        );
    }

    @GetMapping("/vendor/{vendorCode}")
    public ResponseEntity<Object> getByVendor(@PathVariable String vendorCode) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getAllByVendorCode(vendorCode)),
                HttpStatus.OK
        );
    }

    @GetMapping("/plant/{plantId}")
    public ResponseEntity<Object> getByPlant(@PathVariable String plantId) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getAllByPlantId(plantId)),
                HttpStatus.OK
        );
    }

    @GetMapping("/vendor-paginated")
    public ResponseEntity<Object> getPaginatedByVendor(
            @RequestParam String vendorCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<RailInspectionCall> paginatedData = service.getPaginatedCallsByVendor(vendorCode, pageable);
        
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(paginatedData),
                HttpStatus.OK
        );
    }

    @GetMapping("/plant-paginated")
    public ResponseEntity<Object> getPaginatedByPlant(
            @RequestParam String plantId,
            @RequestParam(required = false, defaultValue = "all") String statusType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<RailInspectionCall> paginatedData = service.getPaginatedCallsByPlant(plantId, statusType, pageable);
        
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(paginatedData),
                HttpStatus.OK
        );
    }

    @GetMapping("/plant-completed-paginated")
    public ResponseEntity<Object> getCompletedPaginatedByPlant(
            @RequestParam String plantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<RailInspectionCall> paginatedData = service.getCompletedPaginatedCallsByPlant(plantId, pageable);
        
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(paginatedData),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable Long id) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getById(id)),
                HttpStatus.OK
        );
    }

    @GetMapping("/callNo/{callNo}")
    public ResponseEntity<Object> getByCallNo(@PathVariable String callNo) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getByCallNo(callNo)),
                HttpStatus.OK
        );
    }

    @GetMapping("/ic-details")
    public ResponseEntity<Object> getRailpadIcDetails(@RequestParam String callNo) {
        try {
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(service.getRailpadIcDetails(callNo)),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(new ErrorDetails(500, 1, "ERROR", e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/process-calls")
    public ResponseEntity<Object> getProcessCalls(
            @RequestParam(required = false) String railPadType,
            @RequestParam(required = false) String drawingNo,
            @RequestParam(required = false) String plantId,
            @RequestParam(required = false) String poNo,
            @RequestParam(required = false) String poSr) {
        try {
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(service.getProcessCalls(railPadType, drawingNo, plantId, poNo, poSr)),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(new ErrorDetails(500, 1, "ERROR", e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PutMapping("/modify")
    public ResponseEntity<Object> modifyCall(@RequestBody com.sarthi.SRailPad.dto.RailCallModificationDto dto) {
        try {
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(service.modifyCall(dto)),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(new ErrorDetails(400, 1000, "ERROR", e.getMessage())),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Object> withdrawCall(@RequestBody com.sarthi.SRailPad.dto.RailWithdrawRequestDto dto) {
        try {
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(service.withdrawCall(dto)),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(new ErrorDetails(400, 1000, "ERROR", e.getMessage())),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
