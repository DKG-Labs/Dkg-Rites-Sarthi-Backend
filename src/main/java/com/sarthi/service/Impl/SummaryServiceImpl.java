package com.sarthi.service.Impl;

import com.sarthi.dto.CompanyWiseYearlyData;
import com.sarthi.dto.QuenchingAllDefectsDto;
import com.sarthi.dto.TemperingDefectsDto;
import com.sarthi.dto.reports.*;
import com.sarthi.dto.summaryDtos.*;
import com.sarthi.entity.processmaterial.ProcessLineFinalResult;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.repository.ProcessIeQtyRepository;
import com.sarthi.repository.RmHeatFinalResultRepository;
import com.sarthi.repository.finalmaterial.FinalCumulativeResultsRepository;
import com.sarthi.repository.processmaterial.ProcessLineFinalResultRepository;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SummaryServiceImpl implements SummaryService {

    private final RmHeatFinalResultRepository rawRepo;
    private final ProcessIeQtyRepository processRepo;
    private final FinalCumulativeResultsRepository finalRepo;
    private final InspectionCallRepository inspectionCallRepository;

    private final ProcessIeQtyRepository processIeQtyRepository;
    private final ProcessLineFinalResultRepository processLineFinalResultRepository;

    @Override
    public PageResponseDTO<ManufacturerInspectionSummaryDTO> getDashboard(
            int page,
            int size,
            LocalDate startDate,
            LocalDate endDate, String rio, String zone, String vendor) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Object[]> rawPage = rawRepo.fetchRaw(startDate, endDate, rio, zone, vendor, pageable);
        Page<Object[]> processPage = processRepo.fetchProcess(startDate, endDate, rio, zone, vendor, pageable);
        Page<Object[]> finalPage = finalRepo.fetchFinal(startDate, endDate, rio, zone, vendor, pageable);

        List<ManufacturerInspectionSummaryDTO> combined = Stream.of(rawPage.getContent(),
                processPage.getContent(),
                finalPage.getContent())
                .flatMap(Collection::stream)
                .map(this::mapRow)
                .toList();

        PageResponseDTO<ManufacturerInspectionSummaryDTO> response = new PageResponseDTO<>();

        response.setContent(combined);
        response.setPage(page);
        response.setSize(size);

        long total = rawPage.getTotalElements()
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

        dto.setUsername((String) row[3]); // NEW
        dto.setRio((String) row[4]); // NEW

        dto.setStage((String) row[5]); // shifted

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
            LocalDate endDate, String rio, String zone, String vendor) {

        Pageable pageable = PageRequest.of(page, size);

        // Step 1: Get PO level data using repository
        Page<Object[]> poPage = inspectionCallRepository.fetchMonthlyProgress(startDate, endDate, rio, zone, vendor,
                pageable);



        List<MonthlyProgressReportDTO> content = poPage.getContent()
                .stream()
                .map(this::mapMonthlyRow)
                .toList();

        PageResponseDTO<MonthlyProgressReportDTO> response = new PageResponseDTO<>();

        response.setContent(content);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(poPage.getTotalElements());
        response.setTotalPages(poPage.getTotalPages());

        return response;
    }

    @Override
    public PageResponseDTO<PlantPoWiseDTO> getPlantPoWiseReport(
            int page,
            int size,
            String poiCode,
            LocalDate startDate,
            LocalDate endDate) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Object[]> reportPage =
                inspectionCallRepository.fetchPlantPoWiseSummary(
                        poiCode,
                        startDate,
                        endDate,
                        pageable);

        List<PlantPoWiseDTO> content =
                reportPage.getContent()
                        .stream()
                        .map(this::mapPlantPoWiseRow)
                        .toList();

        PageResponseDTO<PlantPoWiseDTO> response =
                new PageResponseDTO<>();

        response.setContent(content);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(reportPage.getTotalElements());
        response.setTotalPages(reportPage.getTotalPages());

        return response;
    }

    private PlantPoWiseDTO mapPlantPoWiseRow(Object[] row) {

        PlantPoWiseDTO dto = new PlantPoWiseDTO();

        dto.setPlantName(
                row[0] != null ? row[0].toString() : "");

        dto.setNoOfPos(
                row[1] != null
                        ? ((Number) row[1]).longValue()
                        : 0L);

        dto.setPoQty(
                row[2] != null
                        ? ((Number) row[2]).doubleValue()
                        : 0D);

        dto.setRawMaterialAccepted(
                row[3] != null
                        ? ((Number) row[3]).doubleValue()
                        : 0D);

        dto.setProcessInspectionAcceptance(
                row[4] != null
                        ? ((Number) row[4]).doubleValue()
                        : 0D);

        dto.setFinalAcceptance(
                row[5] != null
                        ? ((Number) row[5]).doubleValue()
                        : 0D);

        dto.setTotalFinalAccepted(
                row[6] != null
                        ? ((Number) row[6]).doubleValue()
                        : 0D);

        dto.setBalance(
                row[7] != null
                        ? ((Number) row[7]).doubleValue()
                        : 0D);

        return dto;
    }

    private MonthlyProgressReportDTO mapMonthlyRow(Object[] row) {

        MonthlyProgressReportDTO dto = new MonthlyProgressReportDTO();

        dto.setRly((String) row[0]); // rly_short_name
        dto.setPoNumber((String) row[1]); // po_no
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

        if (row.length > 8 && row[8] != null) {
            if (row[8] instanceof java.sql.Timestamp) {
                dto.setPoDate(((java.sql.Timestamp) row[8]).toLocalDateTime());
            } else if (row[8] instanceof java.time.LocalDateTime) {
                dto.setPoDate((java.time.LocalDateTime) row[8]);
            } else if (row[8] instanceof java.sql.Date) {
                dto.setPoDate(((java.sql.Date) row[8]).toLocalDate().atStartOfDay());
            } else if (row[8] instanceof java.time.LocalDate) {
                dto.setPoDate(((java.time.LocalDate) row[8]).atStartOfDay());
            }
        }

        return dto;
    }

    @Override
    public PageResponseDTO<MonthlyAnalysisDTO> getMonthlyAnalysis(
            int page,
            int size,
            LocalDate startDate,
            LocalDate endDate, String rio, String zone, String vendor) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Object[]> dbPage = inspectionCallRepository.fetchManufacturerSummary(startDate, endDate,
                pageable);

        List<MonthlyAnalysisDTO> content = dbPage.getContent()
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

    @Override
    public PageResponseDTO<PoWiseAnalysisDTO> getPoWiseAnalysis(
            int page,
            int size,
            String poiCode,
            LocalDate startDate,
            LocalDate endDate) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Object[]> dbPage =
                inspectionCallRepository.fetchPoWiseSummary(
                        poiCode,
                        startDate,
                        endDate,
                        pageable);

        System.out.println("Rows fetched = " + dbPage.getContent().size());

        List<PoWiseAnalysisDTO> content = dbPage.getContent()
                .stream()
                .map(this::mapPoWiseRow)
                .toList();

        PageResponseDTO<PoWiseAnalysisDTO> response =
                new PageResponseDTO<>();

        response.setContent(content);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(dbPage.getTotalElements());
        response.setTotalPages(dbPage.getTotalPages());

        return response;
    }

    private PoWiseAnalysisDTO mapPoWiseRow(Object[] row) {

        PoWiseAnalysisDTO dto = new PoWiseAnalysisDTO();

        dto.setRlyZone((String) row[0]);
        dto.setPoNumber((String) row[1]);
        Object poDateObj = row[2];

        if (poDateObj instanceof Timestamp ts) {
            dto.setPoDate(ts.toLocalDateTime().toLocalDate());
        }

        double poQty = getDouble(row[3]);
        double manufactured = getDouble(row[4]);
        double inspected = getDouble(row[5]);
        double rejected = getDouble(row[6]);

        double rmRejected = getDouble(row[7]);
        double processRejected = getDouble(row[8]);
        double finalRejected = getDouble(row[9]);

        dto.setPoQty(poQty);
        dto.setManufactured(manufactured);
        dto.setInspected(inspected);
        dto.setRejected(rejected);

        dto.setRmRejPercent(calcPercent(rmRejected, manufactured));
        dto.setProcessRejPercent(calcPercent(processRejected, manufactured));
        dto.setFinalRejPercent(calcPercent(finalRejected, manufactured));

        return dto;
    }

    @Override
    public PageResponseDTO<CompanyWiseYearlyData> getComapanyWiseMonthlyAnalysis(
            int page,
            int size,
            LocalDate startDate,
            LocalDate endDate,
            String companyName) {

        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusYears(1);
        }

        List<Object[]> results =
                inspectionCallRepository.fetchProcessMonthWiseData(
                        startDate, endDate, companyName);

        List<CompanyWiseYearlyData> content = results.stream()
                .map(this::mapProcessRow)
                .toList();

        PageResponseDTO<CompanyWiseYearlyData> response = new PageResponseDTO<>();
        response.setContent(content);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(content.size());
        response.setTotalPages(1);

        return response;
    }

    /*
    private CompanyWiseYearlyData mapProcessRow(Object[] row) {

        CompanyWiseYearlyData dto = new CompanyWiseYearlyData();

        dto.setManufacturer((String) row[0]);
        dto.setMonth((String) row[1]);

        dto.setInspected(((Number) row[2]).doubleValue());
        dto.setAccepted(((Number) row[3]).doubleValue());
        dto.setProcessRejected(((Number) row[4]).doubleValue());
        dto.setProcessRejPercent(((Number) row[5]).doubleValue());

        // SHEARING (6–9)
        ShearingDefectsDto shearing = new ShearingDefectsDto();
        shearing.setLengthOfCutBar(((Number) row[6]).intValue());
        shearing.setOvalityImproperDiaAtEnd(((Number) row[7]).intValue());
        shearing.setSharpEdges(((Number) row[8]).intValue());
        shearing.setCrackedEdges(((Number) row[9]).intValue());
        dto.setShearingDefects(shearing);

        // TURNING (10–12)
        TurningDefectsDto turning = new TurningDefectsDto();
        turning.setParallelLength(((Number) row[10]).intValue());
        turning.setFullTurningLength(((Number) row[11]).intValue());
        turning.setTurningDia(((Number) row[12]).intValue());
        dto.setTurningDefects(turning);

        // FORGING (13–17)
        ForgingDefectsDto forging = new ForgingDefectsDto();
        forging.setForgingTemperature(((Number) row[13]).intValue());
        forging.setForgingStabilisationRejection(((Number) row[14]).intValue());
        forging.setImproperForging(((Number) row[15]).intValue());
        forging.setForgingMarksNotches(((Number) row[16]).intValue());
        dto.setForgingDefects(forging);

        // TEMPERING (18–19)
        TemperingDefectsDto tempering = new TemperingDefectsDto();
        tempering.setTemperingTemp(((Number) row[18]).intValue());
        tempering.setTemperingDuration(((Number) row[19]).intValue());
        dto.setTemperingDefects(tempering);

        //  QUENCHING (20–24)
        QuenchingAllDefectsDto quenching = new QuenchingAllDefectsDto();
        quenching.setQuenchingTemperatureRejected(((Number) row[20]).intValue());
        quenching.setQuenchingDurationRejected(((Number) row[21]).intValue());
        quenching.setQuenchingHardnessRejected(((Number) row[22]).intValue());
        quenching.setBoxGaugeRejected(((Number) row[23]).intValue());
        quenching.setFlatBearingAreaRejected(((Number) row[24]).intValue());
        dto.setQuenchingDefects(quenching);


        //  FINISHING (26–27)
        FinishingDefectsDto finishing = new FinishingDefectsDto();
        finishing.setPaintIdentification(((Number) row[26]).intValue());
        finishing.setErcCoating(((Number) row[27]).intValue());
        dto.setFinishingDefects(finishing);

        return dto;
    }*/
    private CompanyWiseYearlyData mapProcessRow(Object[] row) {

        CompanyWiseYearlyData dto = new CompanyWiseYearlyData();

        dto.setManufacturer((String) row[0]);
        dto.setMonth((String) row[1]);

        dto.setInspected(getDouble(row[2]));
        dto.setAccepted(getDouble(row[3]));
        dto.setProcessRejected(getDouble(row[4]));
        dto.setProcessRejPercent(getDouble(row[5]));

        // SHEARING
        ShearingDefectsDto shearing = new ShearingDefectsDto();
        shearing.setLengthOfCutBar(getInt(row[6]));
        shearing.setOvalityImproperDiaAtEnd(getInt(row[7]));
        shearing.setSharpEdges(getInt(row[8]));
        shearing.setCrackedEdges(getInt(row[9]));
        dto.setShearingDefects(shearing);

        // TURNING
        TurningDefectsDto turning = new TurningDefectsDto();
        turning.setParallelLength(getInt(row[10]));
        turning.setFullTurningLength(getInt(row[11]));
        turning.setTurningDia(getInt(row[12]));
        dto.setTurningDefects(turning);

        // FORGING
        ForgingDefectsDto forging = new ForgingDefectsDto();
        forging.setForgingTemperature(getInt(row[13]));
        forging.setForgingStabilisationRejection(getInt(row[14]));
        forging.setImproperForging(getInt(row[15]));
        forging.setForgingMarksNotches(getInt(row[16]));
        dto.setForgingDefects(forging);

        // TEMPERING
        TemperingDefectsDto tempering = new TemperingDefectsDto();
        tempering.setTemperingTemp(getInt(row[18]));
        tempering.setTemperingDuration(getInt(row[19]));
        dto.setTemperingDefects(tempering);

        // QUENCHING
        QuenchingAllDefectsDto quenching = new QuenchingAllDefectsDto();
        quenching.setQuenchingTemperatureRejected(getInt(row[20]));
        quenching.setQuenchingDurationRejected(getInt(row[21]));
        quenching.setQuenchingHardnessRejected(getInt(row[22]));
        quenching.setBoxGaugeRejected(getInt(row[23]));
        quenching.setFlatBearingAreaRejected(getInt(row[24]));
        dto.setQuenchingDefects(quenching);

        // FINISHING
        FinishingDefectsDto finishing = new FinishingDefectsDto();
        finishing.setPaintIdentification(getInt(row[26]));
        finishing.setErcCoating(getInt(row[27]));
        dto.setFinishingDefects(finishing);

        return dto;
    }



    private int getInt(Object val) {
        return val == null ? 0 : ((Number) val).intValue();
    }

    private MonthlyAnalysisDTO mapMonthlyAnalysisRow(Object[] row) {

        MonthlyAnalysisDTO dto = new MonthlyAnalysisDTO();

        dto.setManufacturer((String) row[0]);
        dto.setRio((String) row[1]);   // new field

        Double manufactured = getDouble(row[2]);
        Double inspected = getDouble(row[3]);
        Double rejected = getDouble(row[4]);

        Double rmRejected = getDouble(row[5]);
        Double processRejected = getDouble(row[6]);
        Double finalRejected = getDouble(row[7]);

        dto.setManufactured(manufactured);
        dto.setInspected(inspected);
        dto.setRejected(rejected);

        dto.setRmRejected(rmRejected);
        dto.setProcessRejected(processRejected);
        dto.setFinalRejected(finalRejected);

        dto.setRmRejPercent(calcPercent(rmRejected, manufactured));
        dto.setProcessRejPercent(calcPercent(processRejected, manufactured));
        dto.setFinalRejPercent(calcPercent(finalRejected, manufactured));

        // New Fields
        dto.setNoOfPos(row[8] != null ? ((Number) row[8]).longValue() : 0L);
        dto.setPoQty(getDouble(row[9]));
        dto.setUom((String) row[10]);

        return dto;
    }

    private Double calcPercent(Double rejected, Double total) {
        if (total == null || total == 0)
            return 0.0;
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

    @Override
    public PageResponseDTO<MpiaReportDTO> getMpiaReport(int page, int size, LocalDate startDate, LocalDate endDate) {
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : LocalDate.now().minusMonths(6).atStartOfDay();
        LocalDateTime end = (endDate != null) ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        Page<Object[]> dbPage = processLineFinalResultRepository.fetchMpiaReport(start, end, pageable);

        List<MpiaReportDTO> content = dbPage.getContent()
                .stream()
                .map(row -> {
                    MpiaReportDTO dto = new MpiaReportDTO();
                    dto.setManufacture((String) row[0]);
                    dto.setTotalInspected(getDouble(row[1]));
                    dto.setTotalAccepted(getDouble(row[2]));
                    dto.setTotalRejected(getDouble(row[3]));
                    if (dto.getTotalInspected() > 0) {
                        dto.setRejectionPercent((dto.getTotalRejected() * 100.0) / dto.getTotalInspected());
                    } else {
                        dto.setRejectionPercent(0.0);
                    }
                    return dto;
                })
                .toList();

        PageResponseDTO<MpiaReportDTO> response = new PageResponseDTO<>();
        response.setContent(content);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(dbPage.getTotalElements());
        response.setTotalPages(dbPage.getTotalPages());

        return response;
    }

    public List<PlantShiftWiseReportDto> getPlantShiftWiseReport(
            LocalDate startDate,
            LocalDate endDate,
            String poiCode
    ) {

        List<PlantShiftWiseRawDto> results =
                processLineFinalResultRepository.getPlantShiftWiseRawData(
                        startDate,
                        endDate,
                        poiCode
                );

        // GROUP BY DATE + SHIFT
        Map<String, List<PlantShiftWiseRawDto>> grouped =
                results.stream()
                        .collect(Collectors.groupingBy(
                                r -> r.getInspectionDate()
                                        + "_"
                                        + r.getShift()
                        ));

        List<PlantShiftWiseReportDto> response =
                new ArrayList<>();

        for (Map.Entry<String, List<PlantShiftWiseRawDto>> entry
                : grouped.entrySet()) {

            List<PlantShiftWiseRawDto> group =
                    entry.getValue();

            PlantShiftWiseRawDto first =
                    group.get(0);

            PlantShiftWiseReportDto dto =
                    new PlantShiftWiseReportDto();

            dto.setInspectionDate(
                    first.getInspectionDate()
            );

            dto.setShift(
                    first.getShift()
            );

            // LOT NUMBERS
            dto.setLotNumbers(
                    group.stream()
                            .map(PlantShiftWiseRawDto::getLotNumber)
                            .filter(Objects::nonNull)
                            .distinct()
                            .toList()
            );

            // PO NUMBER + SERIAL NUMBER
            dto.setPoNumbers(
                    group.stream()
                            .map(r ->
                                    r.getPoSerialNo()
                            )
                            .distinct()
                            .toList()
            );

            // SHEARING
            dto.setProductionInShearing(
                    group.stream()
                            .mapToInt(r ->
                                    Optional.ofNullable(
                                            r.getShearingManufactured()
                                    ).orElse(0)
                            )
                            .sum()
            );

            // TEMPERING
            dto.setProductionInTempering(
                    group.stream()
                            .mapToInt(r ->
                                    Optional.ofNullable(
                                            r.getTemperingManufactured()
                                    ).orElse(0)
                            )
                            .sum()
            );

            // ACCEPTED
            dto.setAcceptedQtyInTempering(
                    group.stream()
                            .mapToInt(r ->
                                    Optional.ofNullable(
                                            r.getTemperingAccepted()
                                    ).orElse(0)
                            )
                            .sum()
            );

            // REJECTED
            dto.setTotalRejected(
                    group.stream()
                            .mapToInt(r ->
                                    Optional.ofNullable(
                                            r.getTotalRejected()
                                    ).orElse(0)
                            )
                            .sum()
            );

            response.add(dto);
        }

        response.sort(
                Comparator.comparing(
                        PlantShiftWiseReportDto::getInspectionDate
                )
        );

        return response;
    }

    @Override
    public List<Map<String, String>> getPoNumbersByManufacturer(String manufacturer) {
        List<Object[]> rows = inspectionCallRepository.findPoNumbersByManufacturer(manufacturer);
        List<Map<String, String>> response = new ArrayList<>();
        for (Object[] row : rows) {
            String poNo = (String) row[0];
            String rlyCd = (String) row[1];
            Map<String, String> map = new HashMap<>();
            map.put("poNo", poNo);
            map.put("displayPoNo", (rlyCd != null ? rlyCd : "") + " - " + poNo);
            response.add(map);
        }
        return response;
    }

    @Override
    public List<String> getCallNumbersByPoAndManufacturer(String poNo, String manufacturer) {
        return inspectionCallRepository.findCallNumbersByPoNoAndManufacturer(poNo, manufacturer);
    }

}