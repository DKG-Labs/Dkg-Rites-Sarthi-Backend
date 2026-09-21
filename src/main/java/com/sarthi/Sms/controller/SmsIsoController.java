package com.sarthi.Sms.controller;

import com.sarthi.Sms.dto.sms.iso.ChemicalAnalysisIsoResDto;
import com.sarthi.Sms.dto.sms.iso.IsoReportReqDto;
import com.sarthi.Sms.dto.sms.iso.VerificationIsoResDto;
import com.sarthi.Sms.service.SmsIsoService;
import com.sarthi.Sms.util.SmsResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/iso", "/sms/iso"})
public class SmsIsoController {

    @Autowired
    private SmsIsoService smsIsoService;

    @PostMapping("/getHeatDtls")
    public ResponseEntity<Object> getVerificationHeatDtls(@RequestBody IsoReportReqDto req) {
        List<VerificationIsoResDto> res = smsIsoService.getVerificationIsoDtls(req);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping({"/getSmsChemicalAnalysis", "/getChemDtls"})
    public ResponseEntity<Object> getChemicalAnalysisDtls(@RequestBody IsoReportReqDto req) {
        List<ChemicalAnalysisIsoResDto> res = smsIsoService.getChemicalAnalysisIsoDtls(req);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }
}
