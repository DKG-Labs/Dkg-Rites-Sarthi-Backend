package com.sarthi.Sms.service;

import com.sarthi.Sms.dto.sms.common.DutyStatusResDto;
import com.sarthi.Sms.dto.sms.common.EndDutyReqDto;
import com.sarthi.Sms.dto.sms.common.ReportReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyResDto;
import com.sarthi.Sms.dto.welding.*;
import com.sarthi.Sms.entity.welding.TestSampleReportDto;
import com.sarthi.Sms.entity.welding.WeldingDetailEntity;
import java.util.List;

public interface WeldingService {
    StartDutyResDto startDuty(String authHeader, StartDutyReqDto request);
    void endDuty(EndDutyReqDto req);

    public DutyStatusResDto checkDutyStatus(String authHeader);

    public StartDutyResDto getOngoingDutyDtls(String ah);

    void saveWeldingInspection(String authHeader, WeldingInspectionDto requestDto);

    public List<WeldingInspectionResponseDto> getDetailsByPanelDecision();

    void updateWeldingInspection(String authHeader, WeldingInspectionDto requestDto);

    public void saveWeldingTestSample(String authHeader, WeldingTestSampleRequestDto request);

    public void saveWeldingTlt(String authHeader, WeldingTltRequestDto request);

    public void saveWeldingHardness(String authHeader, WeldingHardnessRequestDto request);

    public void saveMicro(WeldingMicroDto req);

    public void saveMacro(WeldingMacroDto req);

    public WeldingInspectionDto getWeldingDtl(Integer id);

    public List<WeldingTestResDto> getPendingTestSampleDtl();

    public List<RecentTestSummaryDto> getRecentTestSummary();

    public List<WeldingMachineWiseCountDto> getMachineWiseTestCount();

    public List<TestSampleReportDto> getTestSampleReport(ReportReqDto req);

    public List<NewWeldReportDto> getNewWeldingReport(ReportReqDto req);

    public List<WeldingSummaryReportDto> getWeldingSummary(ReportReqDto req);

    public List<WeldCombinedDto> getInspectionsByDutyId(String dutyId);

    public void deleteWeldInspection(Integer id);
}
