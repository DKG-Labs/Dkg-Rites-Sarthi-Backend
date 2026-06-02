package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.SleeperDashboardDtos.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public interface DashboardService {

    Long getRejectedSleepersCount();

    Long getTotalRejectedCount();

    Double getRejectionPercentage();

    public List<MonthlyAnalysisDto> getMonthlyAnalysis(String startDate, String endDate);


    public List<LifecycleReportDTO> getLifecycleReport(Long id, String batchNo);


    public List<BatchDTO> getBatches(String plantId);
    public List<CompanyDTO> getCompanies();
    public List<PlantDTO> getPlants(String vendorCode);

    public List<Level5BatchDTO> getBatchChecking(String batchNo, Long batchId);

    public List<Level4BatchDTO> getLevel4Report(String callNo);

    public List<Level3CallDTO> getLevel3Report(String poNo, String srNo);

    public List<Level2DTO> getLevel2(String poNo);

    public List<Level1DTO> getLevel1(LocalDate startDate, LocalDate endDate);

    public List<MprDTO> getMpr(LocalDate startDate, LocalDate endDate);

    public ManufacturerPerformanceResponseDto getLastYearPerformance(
            String plantId);

    public ProcessDefectDistributionResponseDto getProcessDefectDistribution(String plantId);

    public DefectDistributionResponseDto getDefectReasonDistribution(LocalDate fromDate,
            LocalDate toDate);

    public ParetoAnalysisResponseDto getParetoAnalysis(
            LocalDate fromDate,
            LocalDate toDate);

    public List<SleeperEmpPerformanceDto> getEmployeePerformance(
            LocalDate fromDate,
            LocalDate toDate);

}
