package com.sarthi.SRailPad.controller.inspectionCall;

import com.sarthi.SRailPad.dto.RailProcessCallDto;
import com.sarthi.SRailPad.dto.RailProcessCallUpdateDto;
import com.sarthi.SRailPad.dto.RailPoSummaryDto;
import com.sarthi.SRailPad.service.inspectionCall.RailProcessCallService;
import com.sarthi.SRailPad.service.inspectionCall.RailPoSummaryService;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rail-inspection-call/process")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class RailProcessCallController {

    private final RailProcessCallService processCallService;
    private final RailPoSummaryService railPoSummaryService;

    @GetMapping("/{callNo}")
    public ResponseEntity<Object> getProcessCallDetails(@PathVariable String callNo) {
        try {
            RailProcessCallDto dto = processCallService.getProcessCallDetails(callNo);
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(dto),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(new ErrorDetails(404, 1, "NOT_FOUND", e.getMessage())),
                    HttpStatus.NOT_FOUND
            );
        }
    }

    @GetMapping("/available-batches")
    public ResponseEntity<com.sarthi.util.APIResponse> getAvailableBatches(
            @RequestParam String poNo, 
            @RequestParam String railPadType,
            @RequestParam String callNo) {
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(processCallService.getAvailableBatchesForProcessIc(poNo, railPadType, callNo)));
    }

    @PostMapping("/inspect")
    public ResponseEntity<com.sarthi.util.APIResponse> saveInspectionResult(@RequestBody com.sarthi.SRailPad.dto.inspectionCall.ProcessInspectionSaveDto saveDto) {
        processCallService.saveProcessInspectionResult(saveDto);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse("Inspection saved successfully"));
    }

    @GetMapping("/inspect/{callNo}")
    public ResponseEntity<com.sarthi.util.APIResponse> getInspectionResult(@PathVariable String callNo) {
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(processCallService.getProcessInspectionResult(callNo)));
    }

    @PutMapping("/{callNo}")
    public ResponseEntity<Object> updateProcessCallDetails(
            @PathVariable String callNo,
            @RequestBody RailProcessCallUpdateDto updateDto) {
        try {
            RailProcessCallDto dto = processCallService.updateProcessCallDetails(callNo, updateDto);
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(dto),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(new ErrorDetails(400, 1, "UPDATE_FAILED", e.getMessage())),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("/summary/{callNo}")
    public ResponseEntity<Object> getProcessCallSummary(@PathVariable String callNo) {
        try {
            RailProcessCallDto processCall = processCallService.getProcessCallDetails(callNo);
            
            RailPoSummaryDto summary = railPoSummaryService.getSummaryByPoAndSr(processCall.getPoNo(), processCall.getPoSr());

            if (summary == null) {
                summary = new RailPoSummaryDto();
                summary.setPoNo(processCall.getPoNo());
                summary.setRlyPoNo(processCall.getPoNo());
                summary.setPoDate("N/A");
                summary.setVendorCode(processCall.getVendorCode());
                summary.setVendorName("N/A"); // Usually frontend or another service fetches vendor name
                summary.setItemDesc(processCall.getDrawingNo() != null ? "Drawing: " + processCall.getDrawingNo() : "Rail Pad");
                summary.setUnit(processCall.getUom() != null ? processCall.getUom() : "Nos.");
                summary.setPoSrQty(processCall.getQtyOnOrder() != null ? processCall.getQtyOnOrder() : 0);
                summary.setPlaceOfInspection(processCall.getPlantId());
                summary.setMaNo("N/A");
                summary.setMaDate("N/A");
            } else {
                // If drawing number is present, we can append it or use it as item desc
                if (processCall.getDrawingNo() != null) {
                    summary.setItemDesc("Drawing: " + processCall.getDrawingNo() + (summary.getItemDesc() != null ? " (" + summary.getItemDesc() + ")" : ""));
                }
            }
            
            summary.setErcType(processCall.getRailPadType());
            summary.setTotalOfferedQty(processCall.getTotalQty() != null ? processCall.getTotalQty() : processCall.getQtyDesiredForFinal());
            summary.setPoSerialNo(processCall.getPoSr());
            summary.setRlyPoNoSerial(processCall.getPoNo() + " / " + (processCall.getPoSr() != null ? processCall.getPoSr() : "001"));
            summary.setDrawingNo(processCall.getDrawingNo());
            
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(summary),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(new ErrorDetails(404, 1, "NOT_FOUND", e.getMessage())),
                    HttpStatus.NOT_FOUND
            );
        }
    }

    @GetMapping("/available-final-batches/{callNo}")
    public ResponseEntity<com.sarthi.util.APIResponse> getAvailableBatchesForFinal(@PathVariable String callNo) {
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(processCallService.getAvailableBatchesForFinalCall(callNo)));
    }
}
