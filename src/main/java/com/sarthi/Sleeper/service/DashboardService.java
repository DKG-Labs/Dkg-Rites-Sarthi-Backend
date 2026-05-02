package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.SleeperDashboardDtos.*;
import org.springframework.stereotype.Service;

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
    }
