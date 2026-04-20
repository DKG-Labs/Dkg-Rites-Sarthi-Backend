package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.SleeperDashboardDtos.MonthlyAnalysisDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DashboardService {

    Long getRejectedSleepersCount();

    Long getTotalRejectedCount();

    Double getRejectionPercentage();

    public List<MonthlyAnalysisDto> getMonthlyAnalysis(String startDate, String endDate);
}
