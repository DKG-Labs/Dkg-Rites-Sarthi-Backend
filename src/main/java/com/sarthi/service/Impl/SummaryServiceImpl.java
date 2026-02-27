package com.sarthi.service.Impl;

import com.sarthi.dto.summaryDtos.*;
import com.sarthi.repository.ProcessIeQtyRepository;
import com.sarthi.repository.RmHeatFinalResultRepository;
import com.sarthi.repository.finalmaterial.FinalCumulativeResultsRepository;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SummaryServiceImpl implements SummaryService {

    private final RmHeatFinalResultRepository rawRepo;
    private final ProcessIeQtyRepository processRepo;
    private final FinalCumulativeResultsRepository finalRepo;
    private final InspectionCallRepository inspectionCallRepository;

    private final ProcessIeQtyRepository processIeQtyRepository;

    @Override
    public PageResponseDTO<ManufacturerInspectionSummaryDTO> getDashboard(
            int page,
            int size,
            LocalDate startDate,
            LocalDate endDate) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Object[]> rawPage = rawRepo.fetchRaw(startDate, endDate, pageable);
        Page<Object[]> processPage = processRepo.fetchProcess(startDate, endDate, pageable);
        Page<Object[]> finalPage = finalRepo.fetchFinal(startDate, endDate, pageable);

        List<ManufacturerInspectionSummaryDTO> combined =
                Stream.of(rawPage.getContent(),
                                processPage.getContent(),
                                finalPage.getContent())
                        .flatMap(Collection::stream)
                        .map(this::mapRow)
                        .toList();

        PageResponseDTO<ManufacturerInspectionSummaryDTO> response =
                new PageResponseDTO<>();

        response.setContent(combined);
        response.setPage(page);
        response.setSize(size);

        long total =
                rawPage.getTotalElements()
                        + processPage.getTotalElements()
                        + finalPage.getTotalElements();

        response.setTotalElements(total);
        response.setTotalPages((int) Math.ceil((double) total / size));

        return response;
    }


    private ManufacturerInspectionSummaryDTO mapRow(Object[] row) {

        ManufacturerInspectionSummaryDTO dto = new ManufacturerInspectionSummaryDTO();

        dto.setManufacturerId(((Number) row[0]).longValue());
        dto.setManufacturerName((String) row[1]);
        dto.setPoiCode((String) row[2]);

        dto.setUsername((String) row[3]);   // NEW
        dto.setRio((String) row[4]);        // NEW

        dto.setStage((String) row[5]);      // shifted

        Double inspected = getDouble(row[6]);
        Double accepted = getDouble(row[7]);
        Double rejected = getDouble(row[8]);

        dto.setInspectedQty(inspected);
        dto.setAcceptedQty(accepted);
        dto.setRejectedQty(rejected);

        if (inspected != null && inspected > 0) {
            dto.setRejectionPercentage((rejected * 100) / inspected);
        }

        return dto;
    }


    private Double getDouble(Object value) {
        return value == null ? 0.0 : ((Number) value).doubleValue();
    }

    @Override
    public PageResponseDTO<MonthlyProgressReportDTO> getMonthlyProgress(
            int page,
            int size,
            LocalDate startDate,
            LocalDate endDate) {

        Pageable pageable = PageRequest.of(page, size);

        // Step 1: Get PO level data using repository
        Page<Object[]> poPage =
                inspectionCallRepository.fetchMonthlyProgress(startDate, endDate, pageable);

        List<MonthlyProgressReportDTO> content =
                poPage.getContent()
                        .stream()
                        .map(this::mapMonthlyRow)
                        .toList();

        PageResponseDTO<MonthlyProgressReportDTO> response =
                new PageResponseDTO<>();

        response.setContent(content);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(poPage.getTotalElements());
        response.setTotalPages(poPage.getTotalPages());

        return response;
    }

    private MonthlyProgressReportDTO mapMonthlyRow(Object[] row) {

        MonthlyProgressReportDTO dto = new MonthlyProgressReportDTO();

        dto.setRly((String) row[0]);          // rly_short_name
        dto.setPoNumber((String) row[1]);     // po_no
        dto.setManufacturer((String) row[2]); // firm_details

        Double poQty = getDouble(row[3]);
        Double monthlyRm = getDouble(row[4]);
        Double monthlyProcess = getDouble(row[5]);
        Double monthlyFinal = getDouble(row[6]);
        Double totalFinal = getDouble(row[7]);

        dto.setPoQty(poQty);
        dto.setMonthlyRm(monthlyRm);
        dto.setMonthlyProcess(monthlyProcess);
        dto.setMonthlyFinal(monthlyFinal);
        dto.setTotalFinalInspected(totalFinal);

        dto.setPoBalance(poQty - totalFinal);

        return dto;
    }


    @Override
    public PageResponseDTO<MonthlyAnalysisDTO> getMonthlyAnalysis(
            int page,
            int size,
            LocalDate startDate,
            LocalDate endDate) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Object[]> dbPage =
                inspectionCallRepository.fetchManufacturerSummary(startDate, endDate, pageable);

        List<MonthlyAnalysisDTO> content =
                dbPage.getContent()
                        .stream()
                        .map(this::mapMonthlyAnalysisRow)
                        .toList();

        PageResponseDTO<MonthlyAnalysisDTO> response = new PageResponseDTO<>();

        response.setContent(content);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(dbPage.getTotalElements());
        response.setTotalPages(dbPage.getTotalPages());

        return response;
    }

    private MonthlyAnalysisDTO mapMonthlyAnalysisRow(Object[] row) {

        MonthlyAnalysisDTO dto = new MonthlyAnalysisDTO();

        dto.setManufacturer((String) row[0]);

        Double manufactured = getDouble(row[1]);
        Double inspected = getDouble(row[2]);
        Double rejected = getDouble(row[3]);

        Double rmRejected = getDouble(row[4]);
        Double processRejected = getDouble(row[5]);
        Double finalRejected = getDouble(row[6]);

        dto.setManufactured(manufactured);
        dto.setInspected(inspected);
        dto.setRejected(rejected);

        dto.setRmRejected(rmRejected);
        dto.setProcessRejected(processRejected);
        dto.setFinalRejected(finalRejected);

        // Percentages
        dto.setRmRejPercent(calcPercent(rmRejected, manufactured));
        dto.setProcessRejPercent(calcPercent(processRejected, manufactured));
        dto.setFinalRejPercent(calcPercent(finalRejected, manufactured));

        return dto;
    }

    private Double calcPercent(Double rejected, Double total) {
        if (total == null || total == 0) return 0.0;
        return (rejected / total) * 100;
    }

    public List<LotWiseClosedLoopDTO> getClosedLoop(String callNo, String lotNo) {

        List<Object[]> rows = processRepo.getLotClosedLoop(callNo, lotNo);

        List<LotWiseClosedLoopDTO> list = new ArrayList<>();

        for (Object[] r : rows) {

            LotWiseClosedLoopDTO dto = new LotWiseClosedLoopDTO();

            dto.setInspectionDate(((Date) r[0]).toLocalDate());
            dto.setShift((String) r[1]);

            dto.setAccepted(getDouble(r[2]));
            dto.setRejected(getDouble(r[3]));
            dto.setShearing(getDouble(r[4]));
            dto.setTurning(getDouble(r[5]));
            dto.setTempering(getDouble(r[6]));
            dto.setMpi(getDouble(r[7]));
            dto.setForging(getDouble(r[8]));
            dto.setQuenching(getDouble(r[9]));
            dto.setTesting(getDouble(r[10]));

            list.add(dto);
        }

        return list;
    }

    public List<String> getRequestIds(LocalDate startDate, LocalDate endDate) {
        return processRepo.findRequestIdsByDateRange(startDate, endDate);
    }

    public List<String> getLotNumbers(String requestId) {
        return processRepo.findLotNumbersByRequestId(requestId);
    }


}