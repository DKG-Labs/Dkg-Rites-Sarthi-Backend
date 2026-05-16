package com.sarthi.service;

import com.sarthi.dto.PoInspection2ndLevelSerialStatusDto;
import com.sarthi.dto.reports.DashboardSummaryDto;
import com.sarthi.dto.reports.FourthLevelInspectionDto;
import com.sarthi.dto.reports.PoInspection1stLevelStatusDto;
import com.sarthi.dto.reports.PoInspection3rdLevelCallStatusDto;
import com.sarthi.dto.reports.ProcessPerformanceResponseDto;
import com.sarthi.dto.reports.StageRejectionDto;
import com.sarthi.dto.reports.InspectionCallStatusDto;
import com.sarthi.dto.reports.InspectionDetailsDto;
import com.sarthi.dto.reports.PoIssuedDetailDto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface reports {

    public List<PoInspection1stLevelStatusDto> getPoInspection1stLevelStatusList();

    public List<PoInspection2ndLevelSerialStatusDto> getSerialStatusByPoNo(String poNo);

    public List<PoInspection3rdLevelCallStatusDto> getCallWiseStatusBy(String poNo, String serialNo);

    public Page<PoInspection3rdLevelCallStatusDto> getCallWiseStatusBySerialNo(
            String poNo,
            String serialNo,
            int page,
            int size);

    public List<FourthLevelInspectionDto> getFourthLevelReport(String callId);

    public DashboardSummaryDto getDashboardSummary();

    public List<PoIssuedDetailDto> getPoIssuedDetails(String itemCatDescr);

    public List<String> getProcessIcNumbersByUserId(Long userId);

    public double getAvgProductionPerDay();

    public List<StageRejectionDto> getStageWiseRejection();

    public List<StageRejectionDto> getManufacturerRejection();

    public ProcessPerformanceResponseDto getProcessPerformance();

    public List<StageRejectionDto> getDailyRejectionTrend(String startDate, String endDate);

    public List<StageRejectionDto> getManufacturingStepWiseRejection();

    public List<InspectionCallStatusDto> getInspectionCallStatus();

    public List<StageRejectionDto> getParetoAnalysis();

    public List<StageRejectionDto> getMonthlyRejectionTrend(String startDate, String endDate);
    public List<InspectionDetailsDto> getInspectionDetails();

    public List<InspectionDetailsDto> getInspectionDetails(String startDate, String endDate);
    
    public long getSleeperPoCount();
}
