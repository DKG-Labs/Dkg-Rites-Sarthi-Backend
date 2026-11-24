package com.sarthi.Sms.controller;

import com.sarthi.Sms.dto.sms.common.DutyStatusResDto;
import com.sarthi.Sms.dto.sms.common.EndDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyResDto;
import com.sarthi.Sms.dto.shortrail.IdDto;
import com.sarthi.Sms.dto.shortrail.MasterByIdDto;
import com.sarthi.Sms.dto.shortrail.ShortRailMasterDto;
import com.sarthi.Sms.dto.shortrail.ShortRailMasterResDto;
import com.sarthi.Sms.dto.shortrail.ShortRailSummaryResDto;
import com.sarthi.Sms.dto.shortrail.SriRgRsDeleteDto;
import com.sarthi.Sms.dto.shortrail.WorkstationRemarksDto;
import com.sarthi.Sms.service.ShortRailService;
import com.sarthi.Sms.util.SmsResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/sms/shortrailinspection"})
public class ShortRailInspectionController {

    @Autowired
    private ShortRailService shortRailDutyService;

    @PostMapping("/startDuty")
    public ResponseEntity<Object> startDuty(@RequestHeader("Authorization") String authorizationHeader,
            @RequestBody StartDutyReqDto request) {
        StartDutyResDto response = shortRailDutyService.startDuty(authorizationHeader, request);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
    }

    @PostMapping("/endDuty")
    public ResponseEntity<Object> endDuty(@RequestBody EndDutyReqDto request) {
        shortRailDutyService.endDuty(request);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @GetMapping("/checkDutyStatus")
    public ResponseEntity<Object> checkDutyStatus(@RequestHeader("Authorization") String authorizationHeader) {
        DutyStatusResDto response = shortRailDutyService.checkDutyStatus(authorizationHeader);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
    }

    @GetMapping("/getShortRailMasterByDutyId")
    public ResponseEntity<Object> getShortRailMasterByDutyId(@RequestParam String dutyId) {
        List<ShortRailMasterResDto> resDto = shortRailDutyService.getShortRailMasterByDutyId(dutyId);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(resDto), HttpStatus.OK);
    }

    @PostMapping("/save")
    public ResponseEntity<Object> saveShortRailMaster(@RequestBody ShortRailMasterDto dto) {
        shortRailDutyService.saveShortRailMasterDetails(dto);
        return ResponseEntity.ok(SmsResponseBuilder.getSuccessResponse("Short rail master record saved successfully."));
    }
    @PostMapping("/update")
    public ResponseEntity<Object> updateShortRailMaster(@RequestBody ShortRailMasterDto dto) {
        shortRailDutyService.updateShortRailMasterDetails(dto);
        return ResponseEntity.ok(SmsResponseBuilder.getSuccessResponse("Short rail master record saved successfully."));
    }

    @GetMapping("/shortRailSummary")
    public ResponseEntity<Object> getShortRailSummary(@RequestParam("dutyId") String dutyId) {
        List<ShortRailSummaryResDto> summary = shortRailDutyService.getShortRailSummaries(dutyId);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(summary), HttpStatus.OK);
    }

    @GetMapping("/getOngoingDutyDtls")
    public ResponseEntity<Object> getOngoingDutyDtls(@RequestHeader("Authorization") String ah) {
        StartDutyResDto res = shortRailDutyService.getOngoingDutyDtls(ah);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @GetMapping("/getWorkStationRemarks")
    public ResponseEntity<Object> getWorkStationRemarks(@RequestParam String dutyId) {
        List<WorkstationRemarksDto> res = shortRailDutyService.getWorkStationRemarks(dutyId);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/deleteRejectionAnalysisById")
    public ResponseEntity<Object> deleteRejectionAnalysisById(@RequestBody IdDto req) {
        shortRailDutyService.deleteRejectionAnalysisById(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @PostMapping("/getShortRailMasterById")
    public ResponseEntity<Object> getShortRailMasterById(@RequestBody MasterByIdDto req) {
        ShortRailMasterResDto res = shortRailDutyService.getShortRailMasterById(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }


    @PostMapping("/deleteInspectedRgRs")
    public ResponseEntity<Object> deleteInspectedRgRs(@RequestBody SriRgRsDeleteDto req) {
        shortRailDutyService.deleteInspectedRgRs(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }


}
