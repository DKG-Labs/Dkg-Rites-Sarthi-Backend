package com.sarthi.Sms.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sarthi.Sms.dto.sms.common.DutyStatusResDto;
import com.sarthi.Sms.dto.sms.common.EndDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyResDto;
import com.sarthi.Sms.dto.testing.CompletedTestDto;
import com.sarthi.Sms.dto.testing.PendingTestSampleDtlResDto;
import com.sarthi.Sms.entity.testing.CompletedTest;
import com.sarthi.Sms.service.TestingService;
import com.sarthi.Sms.util.SmsResponseBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping({"/sms/testing"})
public class TestingController {

    @Autowired
    private TestingService ts;

     @PostMapping("/startDuty")
    public ResponseEntity<Object> startDuty(@RequestHeader("Authorization") String authHeader, @RequestBody StartDutyReqDto req) {
        StartDutyResDto res = ts.startDuty(authHeader, req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/endDuty")
    public ResponseEntity<Object> endDuty(@RequestBody EndDutyReqDto req) {
        ts.endDuty(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

     @GetMapping("/checkDutyStatus")
    public ResponseEntity<Object> checkDutyStatus(@RequestHeader("Authorization") String authHeader) {
        DutyStatusResDto res = ts.checkDutyStatus(authHeader);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @GetMapping("/getOngoingDutyDtls")
    public ResponseEntity<Object> getOngoingDutyDtls(@RequestHeader("Authorization") String ah) {
        StartDutyResDto res = ts.getOngoingDutyDtls(ah);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }


    @GetMapping("/getPendingTestDtls")
    public ResponseEntity<Object> getPendingTestDtls(@RequestHeader("Authorization") String authHeader) {
        List<PendingTestSampleDtlResDto> res = ts.getPendingTestSampleDtlsByUser(authHeader);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/completeTest")
    public ResponseEntity<Object> completeTest(@RequestBody CompletedTestDto dto) {
        ts.saveCompletedTest(dto);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);

    }

    @GetMapping("/isTestCompleted")
    public ResponseEntity<Object> isTestCompleted(
            @RequestParam String heatNumber,
            @RequestParam Integer strandNumber,
            @RequestParam String sampleId,
            @RequestParam String testType) {
        boolean isCompleted = ts.isTestCompleted(heatNumber, strandNumber, sampleId, testType);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(isCompleted), HttpStatus.OK);
    }

    @GetMapping("/getCompletedTest")
    public ResponseEntity<Object> getCompletedTest(
            @RequestParam String heatNumber,
            @RequestParam Integer strandNumber,
            @RequestParam String sampleId,
            @RequestParam String testType) {
        CompletedTestDto result = ts.getCompletedTest(heatNumber, strandNumber, sampleId, testType);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(result), HttpStatus.OK);
    }

    @GetMapping("/getAllCompletedTests")
    public ResponseEntity<Object> getAllCompletedTests() {
        // Simple endpoint to check if there are any completed tests
        List<CompletedTest> allTests = ts.getAllCompletedTests();
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(allTests), HttpStatus.OK);
    }
    
    @GetMapping("/saveTestRecord")
    public ResponseEntity<Object> saveTestRecord() {
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }
    

    
}
