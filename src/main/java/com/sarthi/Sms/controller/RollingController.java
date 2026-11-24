package com.sarthi.Sms.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sarthi.Sms.constant.RailMillAppConstant;
import com.sarthi.Sms.dto.sms.common.DutyStatusResDto;
import com.sarthi.Sms.dto.sms.common.EndDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyResDto;
import com.sarthi.Sms.exception.SmsErrorDetails;
import com.sarthi.Sms.dto.rolling.ControlHeatReqDto;
import com.sarthi.Sms.dto.rolling.ControlReqDto;
import com.sarthi.Sms.dto.rolling.ControlSampleDtlReqDto;
import com.sarthi.Sms.dto.rolling.FinishingVerificationReqDto;
import com.sarthi.Sms.dto.rolling.HeatAcptTestDtlReqDto;
import com.sarthi.Sms.dto.rolling.HtRailDeleteDto;
import com.sarthi.Sms.dto.rolling.HtSequenceRailIdDto;
import com.sarthi.Sms.dto.rolling.HtSequenceReqDto;
import com.sarthi.Sms.dto.rolling.HtSequenceResDto;
import com.sarthi.Sms.dto.rolling.RailRollingResDto;
import com.sarthi.Sms.dto.rolling.RollingVerificationDto;
import com.sarthi.Sms.dto.rolling.SaveBatchReqDto;
import com.sarthi.Sms.dto.rolling.SaveRailDtlsReqDto;
import com.sarthi.Sms.dto.rolling.SaveRetestSampleReqDto;
import com.sarthi.Sms.dto.rolling.TestSampleAcptDto;
import com.sarthi.Sms.dto.rolling.TestSampleDto;
import com.sarthi.Sms.service.RollingService;
import com.sarthi.Sms.util.SmsResponseBuilder;
import com.sarthi.Sms.dto.sms.DeleteHeatReqDto;
@RestController
@RequestMapping({"/sms/rolling"})
public class RollingController {
    @Autowired
    private RollingService rs;
    @Autowired
    private RollingService rollingService;

    @PostMapping("/startDuty")
    public ResponseEntity<Object> startDuty(@RequestHeader("Authorization") String authHeader, @RequestBody StartDutyReqDto req) {
        StartDutyResDto res = rs.startDuty(authHeader, req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/endDuty")
    public ResponseEntity<Object> endDuty(@RequestBody EndDutyReqDto req) {
        rs.endDuty(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }
    
    @GetMapping("/checkDutyStatus")
    public ResponseEntity<Object> checkDutyStatus(@RequestHeader("Authorization") String authHeader) {
        DutyStatusResDto res = rs.checkDutyStatus(authHeader);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @GetMapping("/getOngoingDutyDtls")
    public ResponseEntity<Object> getOngoingDutyDtls(@RequestHeader("Authorization") String ah) {
        StartDutyResDto res = rs.getOngoingDutyDtls(ah);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @GetMapping("/getControlDtls")
    public ResponseEntity<Object> getControlDtls(@RequestParam String dutyId) {
        RailRollingResDto res = rs.getRollingControlDtls(dutyId);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/getControlSampleDtls")
    public ResponseEntity<Object> getControlSampleDtl(@RequestBody ControlSampleDtlReqDto req) {
        ControlHeatReqDto res = rs.getControlSampleDtl(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/saveControlHeat")
    public ResponseEntity<Object> saveControlHeat(@RequestBody ControlHeatReqDto req) {
        rs.saveControlHeat(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @PostMapping("/saveControlDtls")
    public ResponseEntity<Object> saveControlDtls(@RequestBody ControlReqDto req) {
        rs.saveControlDtls(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @PostMapping("/saveRollingVerification")
    public ResponseEntity<Object> saveRollingVerification(@RequestBody RollingVerificationDto req) {
        rs.saveRollingVerification(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }
    
    @PostMapping("/saveAcceptanceTestSample")
    public ResponseEntity<Object> saveAcceptanceTestSample(@RequestBody TestSampleAcptDto req) {
        rs.saveAcceptanceTestSample(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @PostMapping("/saveRetestSample")
    public ResponseEntity<Object> saveretestSamples(@RequestBody SaveRetestSampleReqDto req) {
        rs.saveRetestSample(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }
    @GetMapping("/getStrandNoList")
    public ResponseEntity<Object> getStrandNoList(@RequestParam String heatNo, @RequestParam String sampleNo) {
        List<Integer> res = rs.getStrandNoList(heatNo, sampleNo);

        Map<String, List<Integer>> resMap = new HashMap<>();
        resMap.put("strandNoList", res);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(resMap), HttpStatus.OK);
    }
    
    @PostMapping("/finishingVerification/save")
    public ResponseEntity<Object> saveFinishingverification(@RequestBody FinishingVerificationReqDto req) {
        rs.saveFinishingVerification(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @PostMapping("/getHeatAcptTestDtls")
    public ResponseEntity<Object> getHeatAcptTestDtls(@RequestBody HeatAcptTestDtlReqDto req) {
        TestSampleAcptDto res = rs.getHeatAcptTestDtls(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/testing/saveRailDtls")
    public ResponseEntity<Object> saveFinishingverification(@RequestBody SaveRailDtlsReqDto req) {
        rs.saveTestingRailDtls(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @PostMapping("/htSequence/saveBatch")
    public ResponseEntity<Object> saveFinishingverification(@RequestBody SaveBatchReqDto req) {
        rs.saveBatch(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @GetMapping("/htSequence/getOpenBatchDtls")
    public ResponseEntity<Object> getOpenBatchDtls() {
        HtSequenceResDto res =  rs.getHtSeqOpenBatchDtls();
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @GetMapping("/htSequence/getRailIdMarkedTests")
    public ResponseEntity<Object> getMarkedTestSamples(@RequestParam String railId) {
        List<String> res =  rs.getMarkedTestSamples(railId);
        Map<String, List<String> > resMap = new HashMap<>();
        resMap.put("markedTests", res);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(resMap), HttpStatus.OK);
    }

    @PostMapping("/htSequence/saveHtSequenceRail")
    public ResponseEntity<Object> saveHtSequenceRail(@RequestBody HtSequenceReqDto req) {
        rs.saveHtSequenceRail(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }
    @PostMapping("/htSequence/deleteRailById")
    public ResponseEntity<Object> deleteHtRail(@RequestBody HtRailDeleteDto req) {
        rs.deleteHtRail(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }
    @PostMapping("/htSequence/updateHtSequenceRail")
    public ResponseEntity<Object> updateHtSequenceRail(@RequestBody HtSequenceReqDto req) {
        rs.updateHtSequenceRail(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @GetMapping("/htSequence/getOpenBatchDtlsV2")
    public ResponseEntity<Object> getOpenBatchDtlsV2() {
        List<HtSequenceRailIdDto> res =  rs.getHtSeqOpenBatchDtlsV2();
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }
    @PostMapping("/deleteHeat")
    public ResponseEntity<Object> deleteHeat(@RequestBody DeleteHeatReqDto req) {
        rollingService.deleteHeatDtl(req);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }


    @GetMapping("/getAllTestSamples")
    public ResponseEntity<Object> getAllTestSamples() {
        List<TestSampleDto> res =  rs.getAllTestSamples();
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
        // return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @GetMapping("/getTestSamplesByCurrentShift")
    public ResponseEntity<Object> getTestSamplesByCurrentShift(@RequestParam String dutyId) {
        List<TestSampleDto> res = rs.getTestSamplesByCurrentShift(dutyId);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @GetMapping("/validateHeatForTesting")
    public ResponseEntity<Object> validateHeatForTesting(@RequestParam String heatNumber) {
        try {
            rs.validateHeatForTesting(heatNumber);
            return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse("Heat number " + heatNumber + " is valid for testing (bloom stage completed)"), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<Object>(SmsResponseBuilder.getErrorResponse(
                new SmsErrorDetails(RailMillAppConstant.ERROR_CODE_VALIDATION, RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION, RailMillAppConstant.ERROR_TYPE_VALIDATION, e.getMessage())
            ), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/deleteTestSample")
    public ResponseEntity<Object> deleteTestSample(@RequestParam String heatNumber, @RequestParam String type) {
        rs.deleteTestSample(heatNumber, type);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }



    @GetMapping("/getTestSampleById")
    public ResponseEntity<Object> getTestSampleById(@RequestParam Integer rollingTestSampleId, @RequestParam String type) {
        TestSampleAcptDto testSample = rs.getTestSampleById(rollingTestSampleId, type);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(testSample), HttpStatus.OK);
    }

    @PostMapping("/updateTestSample")
    public ResponseEntity<Object> updateTestSample(@RequestBody TestSampleAcptDto req) {
        rs.updateTestSample(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @PostMapping("/updateRetestSample")
    public ResponseEntity<Object> updateRetestSample(@RequestBody SaveRetestSampleReqDto req) {
        rs.updateRetestSample(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @PostMapping("/deleteControlHeat")
    public ResponseEntity<Object> deleteControlHeat(@RequestBody ControlHeatReqDto req) {
        //TODO: process POST request
        rollingService.deleteControlHeat(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @GetMapping("/checkRetestSamples")
    public ResponseEntity<Object> checkRetestSamples(@RequestParam String heatNumber) {
        // Debug endpoint to check existing retest samples
        List<Object> retestSamples = rs.checkExistingRetestSamples(heatNumber);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(retestSamples), HttpStatus.OK);
    }

    @DeleteMapping("/forceDeleteRetestSamples")
    public ResponseEntity<Object> forceDeleteRetestSamples(@RequestParam String heatNumber) {
        // Force delete all retest samples for a heat number (for cleanup)
        rs.forceDeleteRetestSamples(heatNumber);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }
    
    // @PostMapping("/rollingVerification/saveBloomYardDtls")
    // public ResponseEntity<Object>  saveBloomYardDtls(@RequestBody VerificationBloomYardReqDto req) {
    //     rs.saveBloomYardDtls(req);
    //     return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    // }

    // @PostMapping("/rollingVerification/saveChargingDtls")
    // public ResponseEntity<Object>  saveChargingDtls(@RequestBody VerificationChargingReqDto req) {
    //     rs.saveChargingDtls(req);
    //     return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    // }

    // @PostMapping("/rollingVerification/saveTempObservationDtls")
    // public ResponseEntity<Object>  saveTempObservationDtls(@RequestBody TempObservationReqResDto req) {
    //     rs.saveTempObservationDtls(req);
    //     return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    // }

    // @GetMapping("/rollingVerification/getTempObservationDtls")
    // public ResponseEntity<Object> getTempObservationDtls(@RequestParam String dutyId) {
    //     List<TempObservationReqResDto> res = rs.getTempObservationDtls(dutyId);
    //     return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    // }

}
