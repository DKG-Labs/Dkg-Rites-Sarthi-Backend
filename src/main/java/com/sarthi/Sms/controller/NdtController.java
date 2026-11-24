package com.sarthi.Sms.controller;

import com.sarthi.Sms.dto.sms.common.DutyStatusResDto;
import com.sarthi.Sms.dto.sms.common.EndDutyReqDto;
import com.sarthi.Sms.dto.sms.common.ReportReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyResDto;
import com.sarthi.Sms.dto.ndt.NdtCalibrationDto;
import com.sarthi.Sms.dto.ndt.NdtCalibrationListResponseDto;
import com.sarthi.Sms.dto.ndt.NdtReportDto;
import com.sarthi.Sms.dto.ndt.NdtShiftSummaryReqDto;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sarthi.Sms.service.NdtService;
import com.sarthi.Sms.util.SmsResponseBuilder;
@RestController
@RequestMapping({"/sms/ndt"})
public class NdtController {

    @Autowired
    private NdtService ndtDutyService;

    @PostMapping("/startDuty")
    public ResponseEntity<Object> startDuty(@RequestHeader("Authorization") String ah,
            @RequestBody StartDutyReqDto req) {
        StartDutyResDto res = ndtDutyService.startDuty(ah, req);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/endDuty")
    public ResponseEntity<Object> endDuty(@RequestBody EndDutyReqDto req) {
        ndtDutyService.endDuty(req);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @GetMapping("/checkDutyProgress")
    public ResponseEntity<Object> checkDutyProgress(@RequestHeader("Authorization") String ah) {
        DutyStatusResDto res = ndtDutyService.checkDutyStatus(ah);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @GetMapping("/getOngoingDutyDtls")
    public ResponseEntity<Object> getOngoingDutyDtls(@RequestHeader("Authorization") String ah) {
        StartDutyResDto res = ndtDutyService.getOngoingDutyDtls(ah);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/saveShiftSummary")
    public ResponseEntity<Object> saveShiftSummary(@RequestBody NdtShiftSummaryReqDto req) {
        ndtDutyService.saveShiftSummary(req);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @GetMapping("/getShiftSummary")
    public ResponseEntity<Object> getShiftSummary(@RequestParam String dutyId ) {
        NdtShiftSummaryReqDto res = ndtDutyService.getShiftSummary(dutyId);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/calibration/saveBulkCalibration")
    public ResponseEntity<Object> saveBulkCalibration(@RequestHeader("Authorization") String ah,
            @RequestBody NdtCalibrationDto requestDto) {
        ndtDutyService.saveCalibrationList(ah, requestDto);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);

    }

    @GetMapping("/calibration/getCalibrationDtls")
    public ResponseEntity<Object> getCalibrationsByDutyId(@RequestParam String dutyId, @RequestParam(name = "prevFilled") Optional<Boolean> prevFilled) {
        if(prevFilled.isPresent()){
            NdtCalibrationListResponseDto res = ndtDutyService.getCalibrationsByDutyId(dutyId, prevFilled.get());
            return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
        }
        NdtCalibrationListResponseDto res = ndtDutyService.getCalibrationsByDutyId(dutyId, false);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/getReport")
    public ResponseEntity<Object> getReport(@RequestBody ReportReqDto req) {
        List<NdtReportDto> res = ndtDutyService.getNdtReport(req);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

}
