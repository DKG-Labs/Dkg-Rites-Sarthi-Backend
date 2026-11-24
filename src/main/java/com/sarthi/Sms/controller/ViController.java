package com.sarthi.Sms.controller;

// import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RestController;

import com.sarthi.Sms.dto.sms.common.DutyStatusResDto;
import com.sarthi.Sms.dto.sms.common.EndDutyReqDto;
import com.sarthi.Sms.dto.sms.common.ReportReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyResDto;
import com.sarthi.Sms.dto.vi.ViAcptRejReportDto;
import com.sarthi.Sms.dto.vi.ViDefectRecordDto;
import com.sarthi.Sms.dto.vi.AcptDataReportDto;
import com.sarthi.Sms.dto.vi.ViDtlsDto;
import com.sarthi.Sms.dto.vi.ViShiftSummaryResDto;
import com.sarthi.Sms.dto.vi.ViShiftSummaryReqDto;
import com.sarthi.Sms.dto.vi.ViSubRailIdEntity;
import com.sarthi.Sms.dto.vi.ViAiComparisonDto;
import com.sarthi.Sms.service.ViService;
import com.sarthi.Sms.util.SmsResponseBuilder;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;


@RestController
@RequestMapping({"/sms/vi"})
public class ViController {

    @Autowired
    private ViService viService;

    @PostMapping("/startDuty")
    public ResponseEntity<Object> startDuty(@RequestHeader("Authorization") String ah, @RequestBody StartDutyReqDto req) {
        StartDutyResDto res = viService.startDuty(ah, req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/endDuty")
    public ResponseEntity<Object> endDuty(@RequestBody EndDutyReqDto req) {
        viService.endDuty(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }
    
    @GetMapping("/checkDutyStatus")
    public ResponseEntity<Object> checkDutyStatus(@RequestHeader("Authorization") String ah) {
        DutyStatusResDto res = viService.checkDutyStatus(ah);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }
    
    @GetMapping("/getOngoingDutyDtls")
    public ResponseEntity<Object> getOngoingDutyDtls(@RequestHeader("Authorization") String ah) {
        StartDutyResDto res = viService.getOngoingDutyDtls(ah);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }
    
    @PostMapping("/saveViDtls")
    // @PostMapping(value = "/saveViDtls", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> saveViDtls(@RequestBody ViDtlsDto req) {
        viService.saveViDtls(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @PostMapping("/getAcptRejReport")
    // @PostMapping(value = "/saveViDtls", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> getAcptRejReport(@RequestBody ReportReqDto req) {
        List<ViAcptRejReportDto> res =  viService.getAcptRejReport(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/getAcptDataReport")
    public ResponseEntity<Object> getAcptDataReport(@RequestBody ReportReqDto req) {
        List<AcptDataReportDto> res =  viService.getAcptDataReport(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/getViDefectAnalysisRecord")
    public ResponseEntity<Object> getViDefectAnalysisRecord(@RequestBody ReportReqDto req) {
        List<ViDefectRecordDto> res =  viService.getViDefectAnalysisRecord(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @GetMapping("/getActOffLengthByRailId")
    public ResponseEntity<Object> getActOffLengthByRailId(@RequestParam String railId) {
        Map<String, BigDecimal> res = viService.getActOffLengthByRailId(railId);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @GetMapping("/getViSummary")
    public ResponseEntity<Object> getShiftSummary(@RequestParam String dutyId, @RequestParam(required = false) String lineNumber) {
        System.out.println("Controller called with dutyId: " + dutyId + ", lineNumber: " + lineNumber);
        List<ViShiftSummaryResDto> res = viService.getShiftSummary(dutyId);
        System.out.println("Raw data size: " + res.size());
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/getViSummaryByDateAndShift")
    public ResponseEntity<Object> getShiftSummaryByDateAndShift(@RequestBody ViShiftSummaryReqDto req) {
        System.out.println("=== VI SUMMARY BY DATE/SHIFT REQUEST ===");
        System.out.println("Date: " + req.getDate());
        System.out.println("Shift: " + req.getShift());
        System.out.println("Line Number: " + req.getLineNumber());
        System.out.println("Line Number is null: " + (req.getLineNumber() == null));

        List<ViShiftSummaryResDto> res = viService.getShiftSummaryByDateAndShift(req);
        System.out.println("Result size: " + res.size());
        System.out.println("=== END REQUEST DEBUG ===");

        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }
    @GetMapping("/getDtlByRailId")
    public ResponseEntity<Object> getDtlByRailId(@RequestParam String railId) {
        ViDtlsDto res= viService.getDtlByRailId(railId);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @GetMapping("/getSubRailIdsByRailId")
    public ResponseEntity<Object> getSubRailIdsByRailId(@RequestParam String railId) {
        List<ViSubRailIdEntity> res = viService.getSubRailIdsByRailId(railId);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @GetMapping("/getHeatNumberByRailId")
    public ResponseEntity<Object> getHeatNumberByRailId(@RequestParam String railId) {
        String heatNumber = viService.getHeatNumberByRailId(railId);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(heatNumber), HttpStatus.OK);
    }

    @GetMapping("/compareViWithAiData")
    public ResponseEntity<Object> compareViWithAiData(@RequestParam String railId) {
        ViAiComparisonDto comparison = viService.compareViWithAiData(railId);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(comparison), HttpStatus.OK);
    }

    @GetMapping("/validateHeatNumberWithBloomStage")
    public ResponseEntity<Object> validateHeatNumberWithBloomStage(
            @RequestParam String railId,
            @RequestParam String heatNumber) {
        Map<String, Object> validationResult = viService.validateHeatNumberWithBloomStage(railId, heatNumber);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(validationResult), HttpStatus.OK);
    }

    @DeleteMapping("/deleteViDtls")
    public ResponseEntity<Object> deleteViDtls(@RequestParam String railId) {
        viService.deleteViDtls(railId);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

}
