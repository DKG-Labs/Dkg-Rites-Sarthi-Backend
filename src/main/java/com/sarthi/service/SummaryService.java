package com.sarthi.service;

import com.sarthi.dto.summaryDtos.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public interface SummaryService {

    public PageResponseDTO<ManufacturerInspectionSummaryDTO> getDashboard(int page, int size, LocalDate startDate,
                                                                          LocalDate endDate) ;

    PageResponseDTO<MonthlyProgressReportDTO> getMonthlyProgress(
            int page,
            int size,
            LocalDate startDate,
            LocalDate endDate);

    public PageResponseDTO<MonthlyAnalysisDTO> getMonthlyAnalysis(
            int page,
            int size,
            LocalDate startDate,
            LocalDate endDate);

    public List<LotWiseClosedLoopDTO> getClosedLoop(String callNo, String lotNo);

    public List<String> getRequestIds(LocalDate startDate, LocalDate endDate);


    public List<String> getLotNumbers(String requestId);





}
