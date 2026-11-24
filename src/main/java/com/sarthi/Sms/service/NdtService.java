package com.sarthi.Sms.service;

import java.util.List;

import com.sarthi.Sms.dto.sms.common.DutyStatusResDto;
import com.sarthi.Sms.dto.sms.common.EndDutyReqDto;
import com.sarthi.Sms.dto.sms.common.ReportReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyResDto;
import com.sarthi.Sms.dto.ndt.NdtCalibrationDto;
import com.sarthi.Sms.dto.ndt.NdtCalibrationListResponseDto;
import com.sarthi.Sms.dto.ndt.NdtReportDto;
import com.sarthi.Sms.dto.ndt.NdtShiftSummaryReqDto;
public interface NdtService {
    StartDutyResDto startDuty(String ah, StartDutyReqDto ndtStartDutyReqDto);

    void endDuty(EndDutyReqDto req);

    public DutyStatusResDto checkDutyStatus(String ah);

    public StartDutyResDto getOngoingDutyDtls(String ah);

    void saveCalibrationList(String ah,NdtCalibrationDto requestDto);

    NdtCalibrationListResponseDto getCalibrationsByDutyId(String dutyId, Boolean prevFilled);

    public void saveShiftSummary(NdtShiftSummaryReqDto shiftSummaryReqDto);

    public List<NdtReportDto> getNdtReport(ReportReqDto req);
    public NdtShiftSummaryReqDto getShiftSummary(String dutyId);
}
