package com.sarthi.service;

import com.sarthi.dto.PoInspection2ndLevelSerialStatusDto;
import com.sarthi.dto.reports.*;
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

    public List<com.sarthi.dto.reports.RailPadVendorWiseQualityDto> getRailPadVendorWiseQualityReport(String startDate, String endDate);

    public List<String> getRailPadDistinctPlants(String vendorCode);

    public List<com.sarthi.dto.reports.RailPadQualityReportDto> getRailPadQualityReport(
            String startDate,
            String endDate);


    public List<String> getAllCompanies();


    public List<com.sarthi.dto.reports.IcAnnexuresReportDto> getDownloadIcAnnexuresReport(String product);

    List<InspectionCallsReportDto> getInspectionCallsReport(String startDate, String endDate);

    public List<InspectionCallsReportDto> getOverduePendingInspectionCallsReport(
            String startDate,
            String endDate);

    public List<IeWiseCallStatusWorkloadSummaryDto> getIeWiseCallStatusWorkloadSummary(
            String cmEmployeeCode);

    public List<IeOperationalSlaPerformanceSummaryDto> getIeOperationalSlaPerformanceSummary(
            String cmEmployeeCode);

    public RailPadFinalInspectionSummaryDto getRailPadFinalInspectionSummary();

    public List<RailPadPoLifeCycle1stLevelDto> getRailPadPo1stLevelStatus();
    public List<RailPadPoLifeCycle2ndLevelDto> getRailPadPo2ndLevelStatus(String poNo);
    public List<RailPadPoLifeCycle3rdLevelDto> getRailPadPo3rdLevelStatus(String poNo, String serialNo);
    public com.sarthi.dto.summaryDtos.PageResponseDTO<com.sarthi.dto.reports.RailPadMprReportDto> getRailPadMprReport(
            int page,
            int size,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            String rio,
            String zone,
            String vendor);

    public com.sarthi.dto.summaryDtos.PageResponseDTO<com.sarthi.dto.reports.RailPadMauReportDto> getRailPadMauReport(
            int page,
            int size,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            String rio,
            String zone,
            String vendor);

    public java.util.List<java.util.Map<String, String>> getRailPadClosedLoopManufacturers();

    public java.util.List<java.util.Map<String, String>> getRailPadClosedLoopPlants(String vendorCode);

    public java.util.List<java.util.Map<String, Object>> getRailPadClosedLoopLots(String plantId, int year);

    public com.sarthi.dto.reports.RailPadLotClosedLoopDto getRailPadLotClosedLoopDetails(Long lotId);

    public com.sarthi.dto.summaryDtos.PageResponseDTO<com.sarthi.dto.summaryDtos.ManufacturerInspectionSummaryDTO> getRailPadPerformanceReport(
            int page,
            int size,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            String rio,
            String zone,
            String vendor);
}

