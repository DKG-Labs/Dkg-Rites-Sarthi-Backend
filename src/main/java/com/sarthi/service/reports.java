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
import com.sarthi.dto.summaryDtos.PoWiseDefectsData;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    public List<StageRejectionDto> getParetoAnalysis(String startDate, String endDate, String product);

    public List<StageRejectionDto> getMonthlyRejectionTrend(String startDate, String endDate);
    public List<StageRejectionDto> getMonthlyRejectionTrend(String startDate, String endDate, String product);
    public List<InspectionDetailsDto> getInspectionDetails();

    public List<InspectionDetailsDto> getInspectionDetails(String startDate, String endDate);
    
    public long getSleeperPoCount();

    public List<PoWiseDefectsData> getPoWiseDefectsReport(
            LocalDate startDate,
            LocalDate endDate);
    public List<com.sarthi.dto.reports.InspectionCallDetailDto> getInspectionCallStatusDetails(String stage, String status);
    public List<com.sarthi.dto.reports.InspectionCallDetailDto> getRailPadInspectionCallStatusDetails(String status);

    public List<com.sarthi.dto.reports.SqcReportDto> getSqcReport();

    public List<com.sarthi.dto.reports.RailPadShiftWiseProductionDto> getRailPadShiftWiseProductionReport(
            String startDate, String endDate, String vendorCode, String plantId);

    public List<java.util.Map<String, String>> getRailPadDistinctVendors();

    public List<String> getRailPadDistinctPlants(String vendorCode);

    public List<com.sarthi.dto.reports.RailPadQualityReportDto> getRailPadQualityReport(
            String startDate,
            String endDate);

    public List<String> getAllCompanies();
}

