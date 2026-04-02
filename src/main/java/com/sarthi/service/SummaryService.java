package com.sarthi.service;

import com.sarthi.dto.CompanyWiseYearlyData;
import com.sarthi.dto.summaryDtos.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public interface SummaryService {

        public PageResponseDTO<ManufacturerInspectionSummaryDTO> getDashboard(int page, int size, LocalDate startDate,
                        LocalDate endDate, String rio, String zone, String vendor);

        PageResponseDTO<MonthlyProgressReportDTO> getMonthlyProgress(
                        int page,
                        int size,
                        LocalDate startDate,
                        LocalDate endDate, String rio, String zone, String vendor);

        public PageResponseDTO<MonthlyAnalysisDTO> getMonthlyAnalysis(
                        int page,
                        int size,
                        LocalDate startDate,
                        LocalDate endDate, String rio, String zone, String vendor);

        public List<LotWiseClosedLoopDTO> getClosedLoop(String callNo, String lotNo);

        public List<String> getRequestIds(LocalDate startDate, LocalDate endDate);

        public List<String> getLotNumbers(String requestId);

        public PageResponseDTO<CompanyWiseYearlyData> getComapanyWiseMonthlyAnalysis(
                int page,
                int size,
                LocalDate startDate,
                LocalDate endDate,
                String companyName);

        public PageResponseDTO<MpiaReportDTO> getMpiaReport(int page, int size, LocalDate startDate, LocalDate endDate);

}
