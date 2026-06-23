package com.sarthi.Sms.controller;


import com.sarthi.Sms.dto.sms.*;
import com.sarthi.Sms.dto.sms.common.*;
import com.sarthi.Sms.service.SmsService;
import com.sarthi.Sms.util.SmsResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/sms")
public class SmsController {

    @Autowired
    private SmsService smsService;
    
    @PostMapping("/startDuty")
    public ResponseEntity<Object> startDuty(@RequestHeader("Authorization") String ah, @RequestBody StartDutyReqDto req) {
        StartDutyResDto res = smsService.startDuty(ah, req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/endDuty")
    public ResponseEntity<Object> endDuty(@RequestBody EndDutyReqDto req) {
        smsService.endDuty(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }
    
    @GetMapping("/checkDutyStatus")
    public ResponseEntity<Object> checkDutyStatus(@RequestHeader("Authorization") String ah) {
        DutyStatusResDto res = smsService.checkDutyStatus(ah);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @GetMapping("/getOngoingDutyDtls")
    public ResponseEntity<Object> getOngoingDutyDtls(@RequestHeader("Authorization") String ah) {
        StartDutyResDto res = smsService.getOngoingDutyDtls(ah);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @GetMapping("/getShiftSummaryDtls")
    public ResponseEntity<Object> getShiftSummaryDtls(@RequestParam String dutyId) {
        ShiftSummaryResDto res = smsService.getSmsShiftSummaryDtls(dutyId);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/saveShiftSummaryDtls")
    public ResponseEntity<Object> saveShiftSummaryDtls(@RequestBody ShiftSummaryReqDto req) {
        smsService.saveShiftSummaryDtls(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @PostMapping("/addNewHeat")
    public  ResponseEntity<Object> addHeat(@RequestBody AddHeatReqDto req) {
        smsService.addNewHeat(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }
    
    @GetMapping("/getHeatDtls")
    public ResponseEntity<Object> getHeatDtls(@RequestParam String heatNo, @RequestParam String dutyId) {
        HeatDtlsResDto res = smsService.getHeatDtls(heatNo, dutyId);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }
    
    @PostMapping("/updateHeatDtls")
    public ResponseEntity<Object> updateHeatDtls(@RequestBody UpdateHeatReqDto req) {
        smsService.updateHeatDtls(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @GetMapping("/getBloomDtls")
    public ResponseEntity<Object> getBloomDtls(@RequestParam String castNo, @RequestParam String dutyId) {
        BloomDtlResDto res = smsService.getBloomDtls(castNo, dutyId);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/saveBloomInsp")
    public ResponseEntity<Object> saveBloomInsp(@RequestBody BloomInspReqDto req) {
        smsService.saveBloomInsp(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @PostMapping("/getSmsSummary")
    public ResponseEntity<Object> getSmsSummary(@RequestBody ReportReqDto req) {
        List<ReportResDto> res = smsService.getSmsReport(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }
    @PostMapping("/getHeatSummary")
    public ResponseEntity<Object> geHeatSummary(@RequestBody ReportReqDto req) {
        List<SmsHeatReportDto> res = smsService.getHeatReport(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/deleteHeat")
    public ResponseEntity<Object> deleteHeat(@RequestBody DeleteHeatReqDto req) {
        smsService.deleteHeatDtl(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @PostMapping("/getStageDtl")
    public ResponseEntity<Object> getStageDtl(@RequestBody StageDtlReqDto req) {
        StageDtlResDto res = smsService.getStageDtl(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }
    
    
}
