package com.sarthi.Sms.service;

import java.util.List;
import java.util.Map;

import com.sarthi.Sms.dto.iso.FilterDto;
import com.sarthi.Sms.dto.iso.RollingControlDto;
import com.sarthi.Sms.dto.iso.RollingFinishingDto;
import com.sarthi.Sms.dto.iso.RollingVerificationDto;
import com.sarthi.Sms.dto.iso.SmsChemicalAnalysisDto;
import com.sarthi.Sms.dto.iso.SmsHeatDtlDto;
import com.sarthi.Sms.dto.iso.SriFinalInspDto;
import com.sarthi.Sms.dto.iso.ViOnlineInspectionDto;
import com.sarthi.Sms.dto.iso.WeldDimCheckDto;
import com.sarthi.Sms.dto.iso.WeldHardnessDtlDto;
import com.sarthi.Sms.dto.iso.WeldJointTestDto;
import com.sarthi.Sms.dto.iso.WeldMicroDtlDto;
import com.sarthi.Sms.dto.iso.WeldingMacroDtlDto;
import com.sarthi.Sms.dto.iso.WeldtltDtlDto;
public interface IsoService {
    public List<SmsHeatDtlDto> getHeatDtls(FilterDto req);
    public List<SmsChemicalAnalysisDto> getSmsChemicalAnalysis(FilterDto req);
    public RollingFinishingDto getRollingFinishingDtls(FilterDto req);
    List<RollingControlDto> getRollingControlDtls(FilterDto req);
    public RollingVerificationDto getRollingVerificationDtls(FilterDto req);
    public List<ViOnlineInspectionDto> getViOnlineInspDtls(FilterDto req);
    public List<WeldDimCheckDto> getWeldDimCheckDtls(FilterDto req);
    public List<WeldJointTestDto> getWeldJointtestDtls(FilterDto req);
    public WeldtltDtlDto getWeldTltDtls(FilterDto req);
    public WeldHardnessDtlDto getWeldHardnesstDtls(FilterDto req);
    public WeldingMacroDtlDto getWeldMacroDtls(FilterDto req);
    public WeldMicroDtlDto getWeldMicroDtls(FilterDto req);
    public SriFinalInspDto getSriFinishingDtls(FilterDto req);
    public Map<String, Object> debugDatabaseQuery(FilterDto req);
}
