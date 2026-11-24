package com.sarthi.Sms.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

import com.sarthi.Sms.dto.iso.FilterDto;
import com.sarthi.Sms.dto.iso.RollingControlDto;
import com.sarthi.Sms.dto.iso.RollingFinishingDto;
import com.sarthi.Sms.dto.iso.RollingVerificationDto;
import com.sarthi.Sms.dto.iso.SmsChemicalAnalysisDto;
import com.sarthi.Sms.dto.iso.SmsHeatDtlDto;
import com.sarthi.Sms.dto.iso.SriFinalInspDto;
import com.sarthi.Sms.dto.iso.ViOnlineInspectionDto;
import com.sarthi.Sms.dto.iso.WeldDimCheckDto;
import com.sarthi.Sms.dto.iso.WeldHardnessDtlDto;
import com.sarthi.Sms.dto.iso.WeldJointTestDto;
import com.sarthi.Sms.dto.iso.WeldMicroDtlDto;
import com.sarthi.Sms.dto.iso.WeldingMacroDtlDto;
import com.sarthi.Sms.dto.iso.WeldtltDtlDto;
import com.sarthi.Sms.service.IsoService;
import com.sarthi.Sms.util.SmsResponseBuilder;
@RestController("railmillIsoController")
@RequestMapping({"/sms/iso"})
public class RailMillIsoController {

    @Autowired
    private IsoService isoService;

    // /getHeatDtls and /getSmsChemicalAnalysis are served by SARTHI's SmsIsoController (same /sms/iso base);
    // they are intentionally not mapped here to avoid ambiguous mappings.

    @PostMapping("/getRollingFinishingDtls")
    public ResponseEntity<Object> getRollingFinishingDtls(@RequestBody FilterDto req) {
        RollingFinishingDto res = isoService.getRollingFinishingDtls(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/getRollingControlDtls")
    public ResponseEntity<Object> getRollingControlDtls(@RequestBody FilterDto req) {
        List<RollingControlDto> res = isoService.getRollingControlDtls(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }
    
    @PostMapping("/getRollingVerificationDtls")
    public ResponseEntity<Object> getRollingVerificationDtls(@RequestBody FilterDto req) {
        RollingVerificationDto res = isoService.getRollingVerificationDtls(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/getViOnlineInspDtls")
    public ResponseEntity<Object> getViOnlineInspDtls(@RequestBody FilterDto req) {
        List<ViOnlineInspectionDto> res = isoService.getViOnlineInspDtls(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/getWeldDimCheckDtls")
    public ResponseEntity<Object> getWeldDimCheckDtls(@RequestBody FilterDto req) {
        List<WeldDimCheckDto> res = isoService.getWeldDimCheckDtls(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }
    @PostMapping("/getWeldJointTestDtls")
    public ResponseEntity<Object> getWeldJointTestDtls(@RequestBody FilterDto req) {
        List<WeldJointTestDto> res = isoService.getWeldJointtestDtls(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }
    @PostMapping("/getWeldTltDtls")
    public ResponseEntity<Object> getWeldTltDtls(@RequestBody FilterDto req) {
        WeldtltDtlDto res = isoService.getWeldTltDtls(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }
    @PostMapping("/getWeldHardnessDtls")
    public ResponseEntity<Object> getWeldHardnessDtls(@RequestBody FilterDto req) {
        WeldHardnessDtlDto res = isoService.getWeldHardnesstDtls(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }
    @PostMapping("/getWeldMacroDtls")
    public ResponseEntity<Object> getWeldMacroDtls(@RequestBody FilterDto req) {
        WeldingMacroDtlDto res = isoService.getWeldMacroDtls(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }
    @PostMapping("/getWeldMicroDtls")
    public ResponseEntity<Object> getWeldMicroDtls(@RequestBody FilterDto req) {
        WeldMicroDtlDto res = isoService.getWeldMicroDtls(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @GetMapping("/debug/weldingData")
    public ResponseEntity<Object> debugWeldingData(@RequestParam(required = false) String jointNo) {
        try {
            Map<String, Object> debugInfo = new HashMap<>();

            if (jointNo != null && !jointNo.isEmpty()) {
                debugInfo.put("jointNumber", jointNo);
                debugInfo.put("message", "Checking data for joint: " + jointNo);

                // Check if joint exists in welding_inspection_detail
                // This would require adding a debug method to the service
                debugInfo.put("status", "Joint number provided for debugging");
            } else {
                debugInfo.put("message", "No joint number provided. Please provide ?jointNo=<joint_number>");
                debugInfo.put("status", "Missing joint number");
            }

            return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(debugInfo), HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            errorInfo.put("status", "Error occurred during debugging");
            return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(errorInfo), HttpStatus.OK);
        }
    }
    @PostMapping("/getSriFinishingInspDtls")
    public ResponseEntity<Object> getSriFinishingInspDtls(@RequestBody FilterDto req) {
        System.out.println("=== API ENDPOINT CALLED ===");
        System.out.println("Request: " + req);
        SriFinalInspDto res = isoService.getSriFinishingDtls(req);
        System.out.println("Response master data: " + res.getMasterDtls());
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @GetMapping("/testSriData")
    public ResponseEntity<Object> testSriData() {
        System.out.println("=== TEST ENDPOINT CALLED ===");
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse("Test endpoint working"), HttpStatus.OK);
    }

    @PostMapping("/debugDatabaseData")
    public ResponseEntity<Object> debugDatabaseData(@RequestBody FilterDto req) {
        System.out.println("=== DATABASE DEBUG ENDPOINT ===");
        System.out.println("Request: " + req);

        Map<String, Object> debugInfo = isoService.debugDatabaseQuery(req);

        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(debugInfo), HttpStatus.OK);
    }



}
