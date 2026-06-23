package com.sarthi.Sms.service;


import com.sarthi.Sms.dto.sms.*;
import com.sarthi.Sms.dto.sms.common.*;
import com.sarthi.Sms.entity.sms.HeatDtlEntity;
import com.sarthi.Sms.entity.sms.HeatDtlSms2Entity;
import com.sarthi.Sms.entity.sms.HeatDtlSms3Entity;

import java.util.List;

public interface SmsService {
    public StartDutyResDto startDuty(String ah, StartDutyReqDto req);

    public void endDuty(EndDutyReqDto req);

    public DutyStatusResDto checkDutyStatus(String ah);

    public ShiftSummaryResDto getSmsShiftSummaryDtls(String dutyId);

    public void saveShiftSummaryDtls(ShiftSummaryReqDto req);

    public void addNewHeat(AddHeatReqDto req);

    public HeatDtlsResDto getHeatDtls(String heatNo, String dutyId);

    public void updateHeatDtls(UpdateHeatReqDto req);

    public BloomDtlResDto getBloomDtls(String castNo, String dutyId);

    public void saveBloomInsp(BloomInspReqDto req);

    public StartDutyResDto getOngoingDutyDtls(String ah);

    public List<ReportResDto> getSmsReport(ReportReqDto req);

    public HeatDtlEntity validateHeatNo(String heatNo);

    public void validateSmsHeat(String heatNo);

    public void validateBloomHeat(String heatNo);

    public List<SmsHeatReportDto> getHeatReport(ReportReqDto req);

    public void deleteHeatDtl(DeleteHeatReqDto req);

    public StageDtlResDto getStageDtl(StageDtlReqDto req);

    public HeatDtlSms3Entity validateSms3Heat(String heatNo);
    
    public HeatDtlSms2Entity validateSms2Heat(String heatNo);
    
    public void validateSms2Sms3Heat(String heatNo);

    public void validateHeatBloomStage(String heatNo);
}
