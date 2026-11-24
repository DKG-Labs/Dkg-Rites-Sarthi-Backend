package com.sarthi.Sms.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sarthi.Sms.dto.sms.common.DutyStatusResDto;
import com.sarthi.Sms.dto.sms.common.EndDutyReqDto;
import com.sarthi.Sms.dto.sms.common.ReportReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyResDto;
import com.sarthi.Sms.dto.welding.NewWeldReportDto;
import com.sarthi.Sms.dto.welding.RecentTestSummaryDto;
import com.sarthi.Sms.dto.welding.WeldCombinedDto;
import com.sarthi.Sms.dto.welding.WeldingHardnessRequestDto;
import com.sarthi.Sms.dto.welding.WeldingInspectionDto;
import com.sarthi.Sms.dto.welding.WeldingInspectionResponseDto;
import com.sarthi.Sms.dto.welding.WeldingMachineWiseCountDto;
import com.sarthi.Sms.dto.welding.WeldingMacroDto;
import com.sarthi.Sms.dto.welding.WeldingMicroDto;
import com.sarthi.Sms.dto.welding.WeldingSummaryReportDto;
import com.sarthi.Sms.dto.welding.WeldingTestResDto;
import com.sarthi.Sms.dto.welding.WeldingTestSampleRequestDto;
import com.sarthi.Sms.dto.welding.WeldingTltRequestDto;
import com.sarthi.Sms.entity.welding.TestSampleReportDto;
import com.sarthi.Sms.service.WeldingService;
import com.sarthi.Sms.service.ViService;
import com.sarthi.Sms.dto.vi.ViSubRailIdEntity;
import com.sarthi.Sms.util.SmsResponseBuilder;
@RestController
@RequestMapping({"/sms/welding"})
public class WeldingController {

    @Autowired
    private WeldingService service;

    @Autowired
    private ViService viService;

    @PostMapping("/startDuty")
    public ResponseEntity<Object> startDuty(@RequestHeader("Authorization") String authHeader, @RequestBody StartDutyReqDto request) {
        StartDutyResDto res=service.startDuty(authHeader, request);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/endDuty")
    public ResponseEntity<Object> endDuty(@RequestBody EndDutyReqDto req) {
        service.endDuty(req);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @GetMapping("/checkDutyStatus")
    public ResponseEntity<Object> checkDutyStatus(@RequestHeader("Authorization") String authHeader) {
        DutyStatusResDto status = service.checkDutyStatus(authHeader);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(status), HttpStatus.OK);
    }

    @GetMapping("/getOngoingDutyDtls")
    public ResponseEntity<Object> getOngoingDutyDtls(@RequestHeader("Authorization") String ah) {
        StartDutyResDto res = service.getOngoingDutyDtls(ah);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/saveWeldInspection")
    public ResponseEntity<Object> saveWeldInspection(@RequestHeader("Authorization") String ah,@RequestBody WeldingInspectionDto requestDto) {

        service.saveWeldingInspection(ah,requestDto);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);

    }
    @GetMapping("/getRefinishHoldingWeldingDtls")
    public ResponseEntity<Object> getDetailsByPanelDecision() {
        List<WeldingInspectionResponseDto> details = service.getDetailsByPanelDecision();
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(details), HttpStatus.OK);
    }

    @GetMapping("/getWeldingInspectionsByDutyId")
public ResponseEntity<Object> getWeldingInspectionsByDutyId(@RequestParam String dutyId) {
    List<WeldCombinedDto> inspections = service.getInspectionsByDutyId(dutyId);
    return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(inspections), HttpStatus.OK);
}
    @PostMapping("/getTestSampleReport")
    public ResponseEntity<Object> getTestSampleReport(@RequestBody ReportReqDto req) {
        List<TestSampleReportDto> res = service.getTestSampleReport(req);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }
    @PostMapping("/getNewWeldingReport")
    public ResponseEntity<Object> getNewWeldingReport(@RequestBody ReportReqDto req) {
        List<NewWeldReportDto> res = service.getNewWeldingReport(req);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }
    @PostMapping("/getWeldingSummaryReport")
    public ResponseEntity<Object> getWeldingSummaryReport(@RequestBody ReportReqDto req) {
        List<WeldingSummaryReportDto> res = service.getWeldingSummary(req);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @GetMapping("/getWeldingDtl")
    public ResponseEntity<Object> getWeldingDtl(@RequestParam Integer id) {
        WeldingInspectionDto res = service.getWeldingDtl(id);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @GetMapping("/getPendingTestSampleDtl")
    public ResponseEntity<Object> getPendingTestSampleDtl() {
        List<WeldingTestResDto> res = service.getPendingTestSampleDtl();
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/updateWeldInspection")
    public ResponseEntity<Object> updateWeldInspection(@RequestHeader("Authorization") String ah,@RequestBody WeldingInspectionDto requestDto) {

        service.updateWeldingInspection(ah,requestDto);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);

    }

    @PostMapping("/saveWeldingTestSample")
    public ResponseEntity<Object> saveWeldingTestSample(@RequestHeader("Authorization") String ah, @RequestBody WeldingTestSampleRequestDto requestDto) {

       service.saveWeldingTestSample(ah,requestDto);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);

    }

    @PostMapping("/saveWeldingTlt")
    public ResponseEntity<Object> saveWeldingTlt(@RequestHeader("Authorization") String ah, @RequestBody WeldingTltRequestDto requestDto) {

        service.saveWeldingTlt(ah,requestDto);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);

    }
    @PostMapping("/saveWeldingHardness")
    public ResponseEntity<Object> saveWeldingHardness(@RequestHeader("Authorization") String ah, @RequestBody WeldingHardnessRequestDto requestDto) {

        service.saveWeldingHardness(ah,requestDto);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);

    }

    @PostMapping("/saveMicro")
    public ResponseEntity<Object> saveMicro(@RequestBody WeldingMicroDto req) {
        service.saveMicro(req);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);

    }

    @PostMapping("/saveMacro")
    public ResponseEntity<Object> saveMacro(@RequestBody WeldingMacroDto req) {
        service.saveMacro(req);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @GetMapping("/getRecentTestSummary")
    public ResponseEntity<Object> getRecentTestSummary() {
       List<RecentTestSummaryDto> res =  service.getRecentTestSummary();
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @GetMapping("/getMachineWiseTestCount")
    public ResponseEntity<Object> getMachineWiseTestCount() {
       List<WeldingMachineWiseCountDto> res =  service.getMachineWiseTestCount();
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/deleteWeldInspection")
    public ResponseEntity<Object> deleteWeldInspection(@RequestParam Integer id) {
        service.deleteWeldInspection(id);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @GetMapping("/getSubRailIdsByRailId")
    public ResponseEntity<Object> getSubRailIdsByRailId(@RequestParam String railId) {
        List<ViSubRailIdEntity> res = viService.getSubRailIdsByRailId(railId);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }
}
