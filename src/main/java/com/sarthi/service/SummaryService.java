package com.sarthi.service;

import com.sarthi.dto.summaryDtos.ManufacturerInspectionSummaryDTO;
import com.sarthi.dto.summaryDtos.MonthlyAnalysisDTO;
import com.sarthi.dto.summaryDtos.MonthlyProgressReportDTO;
import com.sarthi.dto.summaryDtos.PageResponseDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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



}
