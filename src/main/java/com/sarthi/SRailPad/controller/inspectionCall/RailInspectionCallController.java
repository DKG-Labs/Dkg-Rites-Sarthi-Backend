package com.sarthi.SRailPad.controller.inspectionCall;

import com.sarthi.SRailPad.dto.RailPoSummaryDto;
import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall;
import com.sarthi.SRailPad.service.inspectionCall.RailInspectionCallService;
import com.sarthi.SRailPad.service.RailWorkflowService;
import com.sarthi.SRailPad.service.inspectionCall.RailPoSummaryService;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public RailInspectionCallController(RailInspectionCallService service,
                                        RailWorkflowService railWorkflowService,
                                        RailPoSummaryService railPoSummaryService) {
        this.service = service;
        this.railWorkflowService = railWorkflowService;
        this.railPoSummaryService = railPoSummaryService;
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
        }

        // Always overlay Railpad-specific fields from rail_inspection_call
        summary.setErcType(call.getRailPadType());
        summary.setTotalOfferedQty(call.getTotalQty());
        if (call.getPoSr() != null && summary.getPoSerialNo() == null) {
            summary.setPoSerialNo(call.getPoSr());
        }

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(summary),
                HttpStatus.OK
        );
    }

    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody RailInspectionCall call) {
        RailInspectionCall createdCall = service.create(call);
        String callNo = createdCall.getCallNo();

        railWorkflowService.initiateWorkflow(
            callNo,
            0L, // moduleId
            2L, // workflowId
            createdCall.getCreatedBy(),
            createdCall.getVendorCode(),
            createdCall.getPlantId(),
            null // shift
        );

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(callNo),
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
}
