package com.sarthi.Sms.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.sarthi.Sms.dto.sms.common.DutyStatusResDto;
import com.sarthi.Sms.dto.sms.common.EndDutyReqDto;
import com.sarthi.Sms.dto.sms.common.ReportReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyResDto;
import com.sarthi.Sms.dto.vi.AcptDataReportDto;
import com.sarthi.Sms.dto.vi.ViAcptRejReportDto;
import com.sarthi.Sms.dto.vi.ViDefectRecordDto;
import com.sarthi.Sms.dto.vi.ViDtlsDto;
import com.sarthi.Sms.dto.vi.ViShiftSummaryResDto;
import com.sarthi.Sms.dto.vi.ViShiftSummaryReqDto;
import com.sarthi.Sms.dto.vi.ViSubRailIdEntity;
import com.sarthi.Sms.dto.vi.ViSubRailIdWithLengthDto;
import com.sarthi.Sms.dto.vi.ViAiComparisonDto;
public interface ViService {
    public StartDutyResDto startDuty(String ah, StartDutyReqDto viStartDutyReqDto);

    public void endDuty(EndDutyReqDto req);

    public DutyStatusResDto checkDutyStatus(String ah);

    public StartDutyResDto getOngoingDutyDtls(String ah);

    public void saveViDtls(ViDtlsDto req);

    public List<ViAcptRejReportDto> getAcptRejReport(ReportReqDto req);

    public List<AcptDataReportDto> getAcptDataReport(ReportReqDto req);

    public List<ViDefectRecordDto> getViDefectAnalysisRecord(ReportReqDto req);

    public Map<String, BigDecimal> getActOffLengthByRailId(String railId); // Returns accepted length for both parent rail ID and sub rail ID

    public List<ViShiftSummaryResDto> getShiftSummary(String dutyId);

    public List<ViShiftSummaryResDto> getShiftSummaryByLine(String dutyId, String lineNumber);

    // New methods for date and shift-based filtering
    public List<ViShiftSummaryResDto> getShiftSummaryByDateAndShift(ViShiftSummaryReqDto req);

    public ViDtlsDto getDtlByRailId(String railId);

    public List<ViSubRailIdEntity> getSubRailIdsByRailId(String railId);

    public List<ViSubRailIdWithLengthDto> getSubRailIdsWithLengthByRailId(String railId);

    public String getHeatNumberByRailId(String railId);

    public ViAiComparisonDto compareViWithAiData(String railId);

    public Map<String, Object> validateHeatNumberWithBloomStage(String railId, String heatNumber);

    public void deleteViDtls(String railId);
}
