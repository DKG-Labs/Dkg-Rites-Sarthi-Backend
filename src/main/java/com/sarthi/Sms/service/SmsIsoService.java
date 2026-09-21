package com.sarthi.Sms.service;

import com.sarthi.Sms.dto.sms.iso.ChemicalAnalysisIsoResDto;
import com.sarthi.Sms.dto.sms.iso.IsoReportReqDto;
import com.sarthi.Sms.dto.sms.iso.VerificationIsoResDto;

import java.util.List;

public interface SmsIsoService {
    List<VerificationIsoResDto> getVerificationIsoDtls(IsoReportReqDto req);
    List<ChemicalAnalysisIsoResDto> getChemicalAnalysisIsoDtls(IsoReportReqDto req);
}
