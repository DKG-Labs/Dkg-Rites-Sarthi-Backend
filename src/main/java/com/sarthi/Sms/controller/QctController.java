package com.sarthi.Sms.controller;

import com.sarthi.Sms.dto.sms.common.DutyStatusResDto;
import com.sarthi.Sms.dto.sms.common.EndDutyReqDto;
import com.sarthi.Sms.dto.sms.common.ReportReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyResDto;
import com.sarthi.Sms.dto.qct.GetQctInspectionResDto;
import com.sarthi.Sms.dto.qct.PendingTestSummaryDto;
import com.sarthi.Sms.dto.qct.QctCenterLineDto;
import com.sarthi.Sms.dto.qct.QctDtlResDto;
import com.sarthi.Sms.dto.qct.QctFatigueDto;
import com.sarthi.Sms.dto.qct.QctFcgrDto;
import com.sarthi.Sms.dto.qct.QctFractureToughnessDto;
import com.sarthi.Sms.dto.qct.QctNewTestSampleDto;
import com.sarthi.Sms.dto.qct.QctRecordDto;
import com.sarthi.Sms.dto.qct.QctResidualDto;
import com.sarthi.Sms.dto.qct.QctTestingDetailDto;
import com.sarthi.Sms.dto.qct.QctTestingMasterDto;
import com.sarthi.Sms.dto.qct.SaveQctInspectionReqDto;
import com.sarthi.Sms.service.QctService;
import com.sarthi.Sms.util.SmsResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/sms/qct"})
public class QctController {

    @Autowired
    private QctService qs;

    @PostMapping("/startDuty")
    public ResponseEntity<Object> startDuty(@RequestHeader("Authorization") String ah,
            @RequestBody StartDutyReqDto req) {
        StartDutyResDto res = qs.startDuty(ah, req);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/endDuty")
    public ResponseEntity<Object> endDuty(@RequestBody EndDutyReqDto req) {
        qs.endDuty(req);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @GetMapping("/checkDutyProgress")
    public ResponseEntity<Object> checkDutyProgress(@RequestHeader("Authorization") String ah) {
        DutyStatusResDto res = qs.checkDutyStatus(ah);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @GetMapping("/getOngoingDutyDtls")
    public ResponseEntity<Object> getOngoingDutyDtls(@RequestHeader("Authorization") String ah) {
        StartDutyResDto res = qs.getOngoingDutyDtls(ah);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    // @PostMapping("/addQctInspection")
    // public ResponseEntity<Object>
    // addQctInspection(@RequestHeader("Authorization") String authorizationHeader,
    // @RequestBody SaveQctInspectionReqDto requestDto) {
    // qctDutyService.saveQctInspection(authorizationHeader, requestDto);
    // return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(),
    // HttpStatus.OK);
    // }

    // @GetMapping("/getQctInspection")
    // public ResponseEntity<Object> getQctInspection(@RequestParam("dutyId") String
    // dutyId) {
    // GetQctInspectionResDto responseDto = qctDutyService.getQctInspection(dutyId);
    // return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(responseDto),
    // HttpStatus.OK);
    // }

    // @GetMapping("/getAllQctInspections")
    // public ResponseEntity<Object> getAllQctInspections() {
    // List<GetQctInspectionResDto> responseDtos =
    // qctDutyService.getAllQctInspections();
    // return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(responseDtos),
    // HttpStatus.OK);
    // }

    // @PostMapping("/saveQctTestingMaster")
    // public ResponseEntity<Object> saveQctTestingMaster(
    // @RequestHeader("Authorization") String authorizationHeader,
    // @RequestBody QctTestingMasterDto masterDto) {
    // qctDutyService.saveQctTestingMaster(authorizationHeader, masterDto);
    // return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(),
    // HttpStatus.OK);
    // }

    // @PostMapping("/saveQctTestingdetail")
    // public ResponseEntity<Object> saveQctTestingDetail(
    // @RequestHeader("Authorization") String authorizationHeader,
    // @RequestBody QctTestingDetailDto detailDto) {
    // qctDutyService.saveQctTestingDetail(authorizationHeader, detailDto);
    // return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(),
    // HttpStatus.OK);
    // }

    @PostMapping("/saveNewTestSample")
    public ResponseEntity<Object> saveNewTestSample(@RequestBody QctNewTestSampleDto req) {
        qs.saveNewTestSample(req);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @PostMapping("/updateTestSample")
    public ResponseEntity<Object> updateTestSample(@RequestBody QctNewTestSampleDto req) {
        qs.updateTestSample(req);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @PostMapping("/deleteTestSample")
    public ResponseEntity<Object> deleteTestSample(@RequestParam Integer qctId) {
        qs.deleteSample(qctId);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @PostMapping("/saveFatigue")
    public ResponseEntity<Object> saveFatigue(@RequestBody QctFatigueDto req) {
        qs.saveFatigue(req);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @PostMapping("/saveResidual")
    public ResponseEntity<Object> saveResidual(@RequestBody QctResidualDto req) {
        qs.saveResidual(req);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @PostMapping("/saveFcgr")
    public ResponseEntity<Object> saveFcgr(@RequestBody QctFcgrDto req) {
        qs.saveFcgr(req);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @PostMapping("/saveFractureToughness")
    public ResponseEntity<Object> saveFractureToughness(@RequestBody QctFractureToughnessDto req) {
        qs.saveFractureToughness(req);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @PostMapping("/saveCenterLine")
    public ResponseEntity<Object> saveCenterLine(@RequestBody QctCenterLineDto req) {
        qs.saveCenterLine(req);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @GetMapping("/getPendingTestSummary")
    public ResponseEntity<Object> getPendingTestSummary() {
        List<PendingTestSummaryDto> res = qs.getPendingTestSummary();
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @GetMapping("/getQctDtlByDutyId")
    public ResponseEntity<Object> getQctDtlByDutyId(@RequestParam String dutyId) {
        List<QctDtlResDto> res = qs.getQctDtlByDutyId(dutyId);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @GetMapping("/getQctDtlByQctId")
    public ResponseEntity<Object> getQctDtlByQctId(@RequestParam Integer qctId) {
        QctDtlResDto res = qs.getQctDtlByQctId(qctId);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/getQctRecord")
    public ResponseEntity<Object> getQctRecord(@RequestBody ReportReqDto req) {
        List<QctRecordDto> res = qs.getQctRecord(req);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

}
