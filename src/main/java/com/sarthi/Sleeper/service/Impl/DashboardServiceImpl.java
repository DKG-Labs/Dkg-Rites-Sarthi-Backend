package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.SleeperDashboardDtos.*;
import com.sarthi.Sleeper.entity.DemouldingInspection;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCall;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCallBatch;
import com.sarthi.Sleeper.entity.MomentOfResistanceTest;
import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionDeclaration;
import com.sarthi.Sleeper.entity.SleeperPincodePoIMapping;
import com.sarthi.Sleeper.entity.SleeperWorkflowTransaction;
import com.sarthi.Sleeper.entity.VendorPlant;
import com.sarthi.Sleeper.repository.*;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.InspectionTestHeaderRepository;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.InspectionTestResultRepository;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.SleeperInspectionCallRepository;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.WaterCubeStrengthTestRepository;
import com.sarthi.Sleeper.repository.ProductionDeclaration.ProductionDeclarationRepository;
import com.sarthi.Sleeper.repository.ProductionDeclaration.ProductionSleeperRepository;
import com.sarthi.Sleeper.service.DashboardService;
import com.sarthi.dto.reports.IeOperationalSlaPerformanceSummaryDto;
import com.sarthi.dto.reports.IeWiseCallStatusWorkloadSummaryDto;
import com.sarthi.dto.reports.InspectionCallsReportDto;
import com.sarthi.dto.reports.PSCSleeperQualityReportDto;
import jakarta.persistence.Access;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DashboardServiceImpl implements DashboardService {
    @Autowired
    private DemouldingDefectiveSleeperRepository demouldingDefectiveSleeperRepository;

    @Autowired
    private InspectionTestResultRepository inspectionTestResultRepository;
    @Autowired
    private ProductionSleeperRepository productionSleeperRepository;
    @Autowired
    private ProductionDeclarationRepository productionDeclarationRepository;
    @Autowired
    private VendorPlantRepository vendorPlantRepository;
    @Autowired
    private SteamCuringRepository steamCuringRepository;
    @Autowired
    private DemouldingInspectionRepository demouldingInspectionRepository;
    @Autowired
    private WaterCubeStrengthTestRepository waterCubeStrengthTestRepository;
    @Autowired
    private InspectionTestHeaderRepository inspectionTestHeaderRepository;
    @Autowired
    private MomentOfResistanceTestRepository momentOfResistanceTestRepository;

    @Autowired
    private ModulusOfFailureRepository modulusOfFailureRepository;
    @Autowired
    private EpoxyTreatedSleeperRepository epoxyTreatedSleeperRepository;
    @Autowired
    private SleeperPincodePoIMappingRepository sleeperPincodePoIMappingRepository;
    @Autowired
    private SleeperInspectionCallRepository inspectionCallRepository;
    @Autowired
    private SleeperWorkflowRepository sleeperWorkflowRepository;
    @Override
    public Long getRejectedSleepersCount() {
        return demouldingDefectiveSleeperRepository.countByWithReasons();
    }

    @Override
    public Long getTotalRejectedCount() {
        return inspectionTestResultRepository.getTotalRejectedCount();
    }


    @Override
    public Double getRejectionPercentage() {

        Long productionCount = productionSleeperRepository.countBy();
        Long demouldRejected = demouldingDefectiveSleeperRepository.countByWithReasons();
        Long finalRejected = inspectionTestResultRepository.getTotalRejectedCount();

        Long totalRejected = demouldRejected + finalRejected;

        if (productionCount == 0) {
            return 0.0;
        }

        return (totalRejected * 100.0) / productionCount;
    }

    @Override
    public List<MonthlyAnalysisDto> getMonthlyAnalysis(String startDate, String endDate) {


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDateTime start = LocalDate.parse(startDate, formatter).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate, formatter).atTime(23, 59, 59);


        List<Object[]> prodList = productionDeclarationRepository.getProduction(start, end);
        List<Object[]> procList = inspectionTestResultRepository.getProcessRejection(start, end);
        List<Object[]> finalList = inspectionTestResultRepository.getFinalRejection(start, end);

        List<Object[]> masterList = productionDeclarationRepository.getPlantMasterData();


        Map<String, Long> productionMap = new HashMap<>();
        Map<String, Long> processMap = new HashMap<>();
        Map<String, Long> finalMap = new HashMap<>();

        Map<String, String> plantNameMap = new HashMap<>();
        Map<String, String> rioMap = new HashMap<>();

        for (Object[] o : prodList) {
            String plantId = (String) o[0];
            productionMap.put(plantId, ((Number) o[3]).longValue());
        }

        for (Object[] o : procList) {
            processMap.put((String) o[0], ((Number) o[1]).longValue());
        }

        for (Object[] o : finalList) {
            finalMap.put((String) o[0], ((Number) o[1]).longValue());
        }

        for (Object[] o : masterList) {
            String plantId = (String) o[0];
            plantNameMap.put(plantId, (String) o[1]); // company - plantId
            rioMap.put(plantId, (String) o[2]);       // RIO
        }

        List<MonthlyAnalysisDto> result = new ArrayList<>();
        int i = 1;

        for (String plantId : plantNameMap.keySet()) {

            long production = productionMap.getOrDefault(plantId, 0L);
            long process = processMap.getOrDefault(plantId, 0L);
            long finalR = finalMap.getOrDefault(plantId, 0L);

            long acceptance = production - (process + finalR);

            double rejectionPercentage = production == 0 ? 0 :
                    ((process + finalR) * 100.0) / production;

            MonthlyAnalysisDto dto = new MonthlyAnalysisDto();
            dto.setSno(i++);
            dto.setPlantName(plantNameMap.get(plantId)); // company - plantId
            dto.setInspectedBy(rioMap.getOrDefault(plantId, "N/A")); // RIO
            dto.setProduction(production);
            dto.setProcessRejection(process);
            dto.setFinalRejection(finalR);
            dto.setAcceptance(acceptance);
            dto.setRejectionPercentage(rejectionPercentage);

            result.add(dto);
        }

        return result;
    }



    @Override
    public List<LifecycleReportDTO> getLifecycleReport(Long id, String batchNo) {

        List<LifecycleReportDTO> report = new ArrayList<>();

        // Production
        ProductionProjection prod = productionDeclarationRepository.getProductionData(id, batchNo);

        int productionQty = 0;
        if (prod != null) {
            productionQty = prod.getTotalCastedSleepers();

            report.add(new LifecycleReportDTO(
                    "Production Phase",
                    productionQty,
                    prod.getCastingDate(),
                    String.valueOf(prod.getTotalSleeperTypes())
            ));
        }

        // Steam
        SteamProjection steam = steamCuringRepository.getSteamData(batchNo);
        if (steam != null) {
            report.add(new LifecycleReportDTO(
                    "Steam Curing",
                    productionQty,
                    steam.getEntryDate(),
                    String.format("Avg Temp: %.2f°C", steam.getAvgTemp())
            ));
        }

        //Demoulding
        DemouldingProjection demo = demouldingInspectionRepository.getDemouldingData(batchNo);

        int rejectedQty = 0;
        if (demo != null) {
            rejectedQty = demo.getRejectedCount();

            report.add(new LifecycleReportDTO(
                    "Demoulding",
                    productionQty,
                    demo.getInspectionDate(),
                    rejectedQty + " Rejected"
            ));
        }

        int finalQty = productionQty - rejectedQty;

        // Water Cube
        WaterProjection water = waterCubeStrengthTestRepository.getWaterData(batchNo);
        if (water != null) {
            report.add(new LifecycleReportDTO(
                    "Water Cube Testing",
                    finalQty,
                    water.getCreatedDate().toLocalDate(),
                    String.format("Avg Strength: %.2f N/mm²", water.getAvgStrength())
            ));
        }

        // Final Inspection
        FinalInspectionProjection fi = inspectionTestHeaderRepository.getFinalInspectionData(id);
        if (fi != null) {
            report.add(new LifecycleReportDTO(
                    "Final Inspection",
                    finalQty,
                    fi.getTestDate(),
                    fi.getRejectedCount() + " Rejected"
            ));
        }

        // MR
        DateOnlyProjection mr = momentOfResistanceTestRepository.getMRData(batchNo);
        if (mr != null) {
            report.add(new LifecycleReportDTO(
                    "MR Test",
                    finalQty,
                    mr.getCreatedDate().toLocalDate(),
                    null
            ));
        }

        // MF
        DateOnlyProjection mf = modulusOfFailureRepository.getMFData(batchNo);
        if (mf != null) {
            report.add(new LifecycleReportDTO(
                    "MF Test",
                    finalQty,
                    mf.getCreatedDate().toLocalDate(),
                    null
            ));
        }

        // ET
        EtProjection et = epoxyTreatedSleeperRepository.getETData(batchNo);
        if (et != null) {
            report.add(new LifecycleReportDTO(
                    "Epoxy Treatment",
                    finalQty,
                    et.getCreatedDate().toLocalDate(),
                    et.getSleeperCount() + " Treated"
            ));
        }

        //report.sort(Comparator.comparing(LifecycleReportDTO::getDate));
        report.sort(
                Comparator.comparing(
                        LifecycleReportDTO::getDate,
                        Comparator.nullsLast(Comparator.naturalOrder())
                )
        );
        return report;
    }


    @Override
    public List<BatchDTO> getBatches(String plantId) {
        return productionDeclarationRepository.getBatches(plantId)
                .stream()
                .map(b -> new BatchDTO(
                        b.getId(),
                        b.getBatchNumber()))
                .toList();
    }

    @Override
    public List<CompanyDTO> getCompanies() {
        return sleeperPincodePoIMappingRepository.getCompanies()
                .stream()
                .map(c -> new CompanyDTO(
                        c.getCompanyName(),
                        c.getVendorCode()))
                .toList();
    }
@Override
    public List<PlantDTO> getPlants(String vendorCode) {
        return vendorPlantRepository.getPlants(vendorCode)
                .stream()
                .map(p -> new PlantDTO(
                        p.getPlantName(),
                        p.getPlantId()))
                .toList();
    }

    private LocalDate convertToLocalDate(Object dateObj) {
        if (dateObj == null) return null;

        if (dateObj instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        } else if (dateObj instanceof java.util.Date utilDate) {
            return utilDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        } else if (dateObj instanceof Timestamp ts) {
            return ts.toLocalDateTime().toLocalDate();
        }

        throw new IllegalArgumentException("Unsupported date type: " + dateObj.getClass());
    }



    @Override
    public List<Level5BatchDTO> getBatchChecking(String batchNo, Long batchId) {

        List<Object[]> rows = productionDeclarationRepository.getBatchCheckingReport(batchNo, batchId);

        Map<String, Level5BatchDTO> map = new LinkedHashMap<>();

        for (Object[] r : rows) {

            String dateShift = (String) r[0];

            Level5BatchDTO dto = map.getOrDefault(dateShift,
                    new Level5BatchDTO(
                            null,
                            dateShift,
                            null, null, null, null, null, null, null
                    ));

            if (r[1] != null) dto.setSteamCubeStrength(((Number) r[1]).doubleValue());
            if (r[2] != null) dto.setRejectedDemoulding(((Number) r[2]).intValue());
            if (r[3] != null) dto.setRejectedVisual(((Number) r[3]).intValue());
            if (r[4] != null) dto.setRejectedCritical(((Number) r[4]).intValue());
            if (r[5] != null) dto.setRejectedNonCritical(((Number) r[5]).intValue());
            if (r[6] != null) dto.setWaterCubeStrength(((Number) r[6]).doubleValue());
            if (r[7] != null) dto.setMrValue(((Number) r[7]).doubleValue());

            map.put(dateShift, dto);
        }

        AtomicInteger counter = new AtomicInteger(1);

        return map.values().stream()
                .peek(dto -> dto.setSno(counter.getAndIncrement()))
                .toList();
    }


    @Override
    public List<Level4BatchDTO> getLevel4Report(String callNo) {

        List<SleeperInspectionCallBatch> batches =
                inspectionTestHeaderRepository.getBatchesByCallNo(callNo);

        AtomicInteger counter = new AtomicInteger(1);

        return batches.stream().map(b -> {

            ProductionDeclaration pd =
                    productionDeclarationRepository.getProductionByBatch(b.getBatchNo());

            int total = pd != null ? pd.getTotalCastedSleepers() : 0;

            int rejected = b.getBadSleepers() != null ? b.getBadSleepers().size() : 0;
            int passed = b.getGoodSleepers() != null ? b.getGoodSleepers().size() : 0;

            return new Level4BatchDTO(
                    counter.getAndIncrement(),
                    b.getBatchNo(),
                    pd != null ? pd.getCastingDate() : null,
                    total,
                    total,
                    rejected,
                    passed
            );

        }).toList();
    }

    @Override
    public List<Level3CallDTO> getLevel3Report(String poNo, String srNo) {

        List<SleeperInspectionCall> calls = inspectionCallRepository.getCalls(poNo, srNo);

        AtomicInteger counter = new AtomicInteger(1);

        return calls.stream().map(call -> {

            int offered = call.getTotalOffered() != null ? call.getTotalOffered() : 0;

            int accepted = 0;
            int rejected = 0;

            if (call.getBatchesSelected() != null) {
                for (SleeperInspectionCallBatch batch : call.getBatchesSelected()) {

                    if (batch.getGoodSleepers() != null)
                        accepted += batch.getGoodSleepers().size();

                    if (batch.getBadSleepers() != null)
                        rejected += batch.getBadSleepers().size();
                }
            }

            double rejectionPercent = offered > 0
                    ? (rejected * 100.0) / offered
                    : 0.0;

            return new Level3CallDTO(
                    counter.getAndIncrement(),
                    call.getCallNo(),
                    null, // Des Date
                    offered,
                    accepted,
                    rejected,
                    Math.round(rejectionPercent * 100.0) / 100.0,
                    null // IC No
            );

        }).toList();
    }

    public List<Level2DTO> getLevel2(String poNo) {

        List<Level2Projection> data = productionDeclarationRepository.getLevel2Data(poNo);

        AtomicInteger counter = new AtomicInteger(1);

        return data.stream().map(d -> {

            int qty = d.getQty() != null ? d.getQty() : 0;

            int accepted = d.getTotalAccepted() != null ? d.getTotalAccepted() : 0;
            int processRejected = d.getProcessRejected() != null ? d.getProcessRejected() : 0;
            int finalRejected = d.getFinalRejected() != null ? d.getFinalRejected() : 0;


            int balance = qty - accepted;


            double procRejPercent = qty > 0
                    ? (processRejected * 100.0) / qty
                    : 0.0;

            double finalRejPercent = qty > 0
                    ? (finalRejected * 100.0) / qty
                    : 0.0;

            double totalRejPercent = procRejPercent + finalRejPercent;

            return new Level2DTO(
                    counter.getAndIncrement(),
                    d.getPoNo(),
                    d.getSrNo(),
                    null,
                    d.getConsignee(),

                    d.getDeliveryDate() != null ? d.getDeliveryDate().toLocalDate() : null,
                    d.getExtendedDeliveryDate() != null ? d.getExtendedDeliveryDate().toLocalDate() : null,

                    qty + " " + (d.getUom() != null ? d.getUom() : ""),

                    balance,
                    null,
                    null,

                    round(procRejPercent),
                    round(finalRejPercent),
                    round(totalRejPercent)
            );

        }).toList();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    @Override
    public List<Level1DTO> getLevel1(LocalDate startDate, LocalDate endDate) {

        List<Level1Projection> data = inspectionCallRepository.getLevel1Data(startDate, endDate);

        AtomicInteger counter = new AtomicInteger(1);

        return data.stream().map(d -> {

            int poQty = d.getPoQty() != null ? d.getPoQty() : 0;
            int accQty = d.getAccQty() != null ? d.getAccQty() : 0;

            int totalRejected = d.getTotalRejected() != null ? d.getTotalRejected() : 0;
            int totalOffered = d.getTotalOffered() != null ? d.getTotalOffered() : 0;

            // Balance
            int balQty = poQty - accQty;

            //  Rejection %
            double rejectionPercent = totalOffered > 0
                    ? (totalRejected * 100.0) / totalOffered
                    : 0.0;

            return new Level1DTO(
                    counter.getAndIncrement(),
                    d.getRly(),
                    d.getPoNo(),
                    d.getPoDate() != null ? d.getPoDate().toLocalDateTime().toLocalDate() : null,
                    d.getVendor(),
                    d.getRegion(),

                    poQty,
                    accQty,
                    balQty,

                    round(rejectionPercent),
                    d.getUom()
            );

        }).toList();
    }

    public List<MprDTO> getMpr(LocalDate startDate, LocalDate endDate) {

        List<MprProjection> data = inspectionCallRepository.getMprData(startDate, endDate);

        AtomicInteger counter = new AtomicInteger(1);

        return data.stream().map(d -> {

            int poQty = d.getPoQty() != null ? d.getPoQty() : 0;
            int totalDispatched = d.getTotalDispatched() != null ? d.getTotalDispatched() : 0;

            int balance = poQty - totalDispatched;

            return new MprDTO(
                    counter.getAndIncrement(),
                    d.getRly(),
                    d.getPoNo(),
                    d.getManufacturer(),
                    poQty,
                    d.getDispatchedInPeriod(),
                    totalDispatched,
                    balance
            );

        }).toList();
    }

    @Override
    public ManufacturerPerformanceResponseDto getLastYearPerformance(
            String plantId) {

        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusYears(1);

        List<Object[]> rows =
                productionDeclarationRepository.getMonthlyPerformance(
                        plantId,
                        fromDate,
                        toDate
                );

        List<MonthlyPerformanceDto> monthlyList = new ArrayList<>();

        long totalInspected = 0;
        long totalRejected = 0;

        for (Object[] row : rows) {

            String month = (String) row[0];

            Long inspected =
                    row[1] != null
                            ? ((Number) row[1]).longValue()
                            : 0L;

            Long rejected =
                    row[2] != null
                            ? ((Number) row[2]).longValue()
                            : 0L;

            Double percentage = 0.0;

            if (inspected > 0) {
                percentage =
                        (rejected * 100.0) / inspected;
            }

            monthlyList.add(
                    new MonthlyPerformanceDto(
                            month,
                            inspected,
                            rejected,
                            Math.round(percentage * 100.0) / 100.0
                    )
            );

            totalInspected += inspected;
            totalRejected += rejected;
        }

        ManufacturerPerformanceResponseDto response =
                new ManufacturerPerformanceResponseDto();

        response.setMonthlyPerformance(monthlyList);

        response.setTotalInspected(totalInspected);

        response.setTotalRejected(totalRejected);

        double avg = 0.0;

        if (totalInspected > 0) {
            avg = (totalRejected * 100.0) / totalInspected;
        }

        response.setAverageRejectionPercentage(
                Math.round(avg * 100.0) / 100.0
        );

        return response;
    }

    @Override
    public ProcessDefectDistributionResponseDto
    getProcessDefectDistribution(String plantId) {

        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusYears(1);

        List<Object[]> rows =
               inspectionTestHeaderRepository.getProcessDefectDistribution(
                        plantId,
                        fromDate,
                        toDate
                );

        List<DefectDistributionDto> defectList =
                new ArrayList<>();

        long totalDefects = 0;

        for (Object[] row : rows) {

            Number count = (Number) row[1];

            totalDefects += count.longValue();
        }

        for (Object[] row : rows) {

            String defectName = (String) row[0];

            Long defectCount =
                    ((Number) row[1]).longValue();

            Double percentage = 0.0;

            if (totalDefects > 0) {

                percentage =
                        (defectCount * 100.0) / totalDefects;
            }

            defectList.add(
                    new DefectDistributionDto(
                            defectName,
                            defectCount,
                            Math.round(percentage * 100.0) / 100.0
                    )
            );
        }

        ProcessDefectDistributionResponseDto response =
                new ProcessDefectDistributionResponseDto();

        response.setDefects(defectList);

        return response;
    }


    @Override
    public DefectDistributionResponseDto
    getDefectReasonDistribution(
            LocalDate fromDate,
            LocalDate toDate) {

        List<Object[]> rows =
                inspectionTestHeaderRepository.getDefectReasonDistribution(
                        fromDate,
                        toDate
                );

        List<DefectReasonDistributionDto> list =
                new ArrayList<>();

        long total = 0;

        for (Object[] row : rows) {

            total += ((Number) row[2]).longValue();
        }

        for (Object[] row : rows) {

            String category = (String) row[0];

            String defectReason = (String) row[1];

            Long defectCount =
                    ((Number) row[2]).longValue();

            Double percentage = 0.0;

            if (total > 0) {

                percentage =
                        (defectCount * 100.0) / total;
            }

            list.add(
                    new DefectReasonDistributionDto(
                            category,
                            defectReason,
                            defectCount,
                            Math.round(percentage * 100.0) / 100.0
                    )
            );
        }

        DefectDistributionResponseDto response =
                new DefectDistributionResponseDto();

        response.setDefects(list);

        return response;
    }

    @Override
    public ParetoAnalysisResponseDto
    getParetoAnalysis(
            LocalDate fromDate,
            LocalDate toDate) {

        List<Object[]> rows =
                inspectionTestHeaderRepository.getParetoAnalysis(
                        fromDate,
                        toDate
                );

        List<ParetoAnalysisDto> list =
                new ArrayList<>();

        long total = 0;

        for (Object[] row : rows) {

            total += ((Number) row[1]).longValue();
        }

        double cumulative = 0.0;

        for (Object[] row : rows) {

            String category = (String) row[0];

            Long count =
                    ((Number) row[1]).longValue();

            Double percentage = 0.0;

            if (total > 0) {

                percentage =
                        (count * 100.0) / total;
            }

            cumulative += percentage;

            list.add(
                    new ParetoAnalysisDto(
                            category,
                            count,
                            Math.round(percentage * 100.0) / 100.0,
                            Math.round(cumulative * 100.0) / 100.0
                    )
            );
        }

        ParetoAnalysisResponseDto response =
                new ParetoAnalysisResponseDto();

        response.setDefects(list);

        return response;
    }

    @Override
    public List<SleeperEmpPerformanceDto> getEmployeePerformance(
            LocalDate fromDate,
            LocalDate toDate) {

        List<SleeperEmpPerformanceDto> response = new ArrayList<>();


        // ================= PROCESS =================

        List<Object[]> processList =
                demouldingInspectionRepository
                        .getProcessInspectionReport(fromDate, toDate);

        for (Object[] row : processList) {

            SleeperEmpPerformanceDto dto =
                    new SleeperEmpPerformanceDto();

            dto.setCompanyName(
                    row[0] != null
                            ? String.valueOf(row[0])
                            : null
            );

            dto.setPlantName(
                    row[1] != null
                            ? String.valueOf(row[1])
                            : null
            );

            dto.setPlantId(
                    row[2] != null
                            ? String.valueOf(row[2])
                            : null
            );

            dto.setRio(
                    row[3] != null
                            ? String.valueOf(row[3])
                            : null
            );

            dto.setIeName(
                    row[4] != null
                            ? String.valueOf(row[4])
                            : null
            );

            dto.setStageOfInspection(
                    row[5] != null
                            ? String.valueOf(row[5])
                            : null
            );

            dto.setShift(
                    row[6] != null
                            ? String.valueOf(row[6])
                            : null
            );

            dto.setShiftsWorked(
                    row[7] != null
                            ? ((Number) row[7]).longValue()
                            : 0L
            );

            dto.setRejectedSleepers(
                    row[8] != null
                            ? ((Number) row[8]).longValue()
                            : 0L
            );

            response.add(dto);
        }


        // ================= FINAL =================

        List<Object[]> finalList =
                inspectionTestHeaderRepository
                        .getFinalInspectionReport(fromDate, toDate);

        for (Object[] row : finalList) {

            SleeperEmpPerformanceDto dto =
                    new SleeperEmpPerformanceDto();

            dto.setCompanyName(
                    row[0] != null
                            ? String.valueOf(row[0])
                            : null
            );

            dto.setPlantName(
                    row[1] != null
                            ? String.valueOf(row[1])
                            : null
            );

            dto.setPlantId(
                    row[2] != null
                            ? String.valueOf(row[2])
                            : null
            );

            dto.setRio(
                    row[3] != null
                            ? String.valueOf(row[3])
                            : null
            );

            dto.setIeName(
                    row[4] != null
                            ? String.valueOf(row[4])
                            : null
            );

            dto.setStageOfInspection(
                    row[5] != null
                            ? String.valueOf(row[5])
                            : null
            );

            dto.setShift(
                    row[6] != null
                            ? String.valueOf(row[6])
                            : null
            );

            dto.setShiftsWorked(
                    row[7] != null
                            ? ((Number) row[7]).longValue()
                            : 0L
            );

            dto.setRejectedSleepers(
                    row[8] != null
                            ? ((Number) row[8]).longValue()
                            : 0L
            );

            response.add(dto);
        }

        return response;
    }


        public List<ShiftWiseProductionReportDto> getReport(  LocalDate fromDate,
                                                              LocalDate toDate , String plantId)  {



        List<Object[]> rows =
                        productionDeclarationRepository.getShiftWiseProductionReport(
                                fromDate,
                                toDate,
                                plantId
                        );

                List<ShiftWiseProductionReportDto> response =
                        new ArrayList<>();

                for (Object[] row : rows) {

                    ShiftWiseProductionReportDto dto =
                            new ShiftWiseProductionReportDto();

                    dto.setDate(row[0] != null ? row[0].toString() : null);

                    dto.setShift(row[1] != null ? row[1].toString() : null);

                    dto.setLineOrShedNo(row[2] != null ? row[2].toString() : null);

                    dto.setNoOfBatches(
                            row[3] != null
                                    ? ((Number) row[3]).longValue()
                                    : 0L
                    );

                    dto.setNoOfSleepers(
                            row[4] != null
                                    ? ((Number) row[4]).intValue()
                                    : 0
                    );

                    dto.setSleeperTypesAndCounts(
                            row[5] != null ? row[5].toString() : ""
                    );

                    dto.setProcessRejectedSleepers(
                            row[6] != null
                                    ? ((Number) row[6]).longValue()
                                    : 0L
                    );
                    dto.setFinalRejectedSleepers(
                            row[7] != null
                                    ? ((Number) row[7]).longValue()
                                    : 0L
                    );

                    dto.setEtRejectedSleepers(
                            row[8] != null
                                    ? ((Number) row[8]).longValue()
                                    : 0L
                    );
                    response.add(dto);
                }

                return response;
            }



    public List<PSCSleeperQualityReportDto> getQtyPscSleeperReport(
            LocalDate startDate,
            LocalDate endDate
    ) {

        List<Object[]> rows =
                productionDeclarationRepository.getPSCSleeperQualityReport(
                        startDate,
                        endDate
                );

        List<PSCSleeperQualityReportDto> response =
                new ArrayList<>();

        for (Object[] row : rows) {

            PSCSleeperQualityReportDto dto =
                    new PSCSleeperQualityReportDto();

            dto.setCse(
                    row[0] != null ? row[0].toString() : ""
            );

            dto.setPlantId(
                    row[1] != null ? row[1].toString() : ""
            );

            dto.setSleeperType(
                    row[2] != null ? row[2].toString() : ""
            );

            dto.setNoOfSleepersProducedDuringMonth(
                    row[3] != null
                            ? ((Number) row[3]).longValue()
                            : 0L
            );

            response.add(dto);
        }

        return response;
    }

    @Override
    public List<String> getVendorPlantCompanyNames() {
        return vendorPlantRepository.findDistinctCompanyNames();
    }

    @Override
    public List<PlantDTO> getVendorPlantsByCompanyName(String companyName) {
        return vendorPlantRepository.findPlantsByCompanyName(companyName);
    }


    public List<InspectionCallsReportDto> getInspectionCallsReport(
            String startDate,
            String endDate) {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate start =
                (startDate != null && !startDate.isBlank())
                        ? LocalDate.parse(startDate, formatter)
                        : LocalDate.now().minusYears(1);

        LocalDate end =
                (endDate != null && !endDate.isBlank())
                        ? LocalDate.parse(endDate, formatter)
                        : LocalDate.now();

        LocalDateTime startDateTime =
                start.atStartOfDay();

        LocalDateTime endDateTime =
                end.atTime(23, 59, 59);

        List<Object[]> results =
                            inspectionCallRepository.getSleeperInspectionReport(startDateTime, endDateTime);

        return results.stream().map(obj -> {

            InspectionCallsReportDto dto =
                    new InspectionCallsReportDto();

            dto.setCallNumber((String) obj[0]);
            dto.setProductAndStageOfInspection((String) obj[1]);
            dto.setPoNumber((String) obj[2]);

            dto.setDeliveryDate(
                    obj[3] != null
                            ? ((Timestamp) obj[3]).toLocalDateTime()
                            : null
            );

            dto.setExpectedDeliveryDate(
                    obj[4] != null
                            ? ((Timestamp) obj[4]).toLocalDateTime()
                            : null
            );

            dto.setVendorName((String) obj[5]);

            dto.setInspectionDesiredDate((LocalDate) obj[6]);

            dto.setCallDate(
                    obj[7] != null
                            ? ((Timestamp) obj[7]).toLocalDateTime()
                            : null
            );

            dto.setIeName(
                    obj[8] != null ? obj[8].toString() : null
            );

            dto.setCmName((String) obj[9]);

            dto.setRitesRio((String) obj[10]);

            dto.setStatus((String) obj[11]);

            return dto;

        }).toList();
    }


    @Override
    public List<InspectionCallsReportDto> getSleeperOverduePendingCallsReport(
            String startDate,
            String endDate) {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate start =
                (startDate != null && !startDate.isBlank())
                        ? LocalDate.parse(startDate, formatter)
                        : LocalDate.now().minusYears(1);

        LocalDate end =
                (endDate != null && !endDate.isBlank())
                        ? LocalDate.parse(endDate, formatter)
                        : LocalDate.now();

        LocalDateTime startDateTime =
                start.atStartOfDay();

        LocalDateTime endDateTime =
                end.atTime(23, 59, 59);

        List<Object[]> results =
                inspectionCallRepository
                        .getSleeperOverduePendingCallsReport(
                                startDateTime,
                                endDateTime
                        );

        return results.stream().map(obj -> {

            InspectionCallsReportDto dto =
                    new InspectionCallsReportDto();

            dto.setCallNumber(
                    obj[0] != null ? obj[0].toString() : null
            );

            dto.setProductAndStageOfInspection(
                    obj[1] != null ? obj[1].toString() : null
            );

            dto.setPoNumber(
                    obj[2] != null ? obj[2].toString() : null
            );

            dto.setDeliveryDate(null);

            dto.setExpectedDeliveryDate(null);

            dto.setVendorName(
                    obj[5] != null ? obj[5].toString() : null
            );

            dto.setInspectionDesiredDate(
                    obj[6] != null
                            ? ((java.sql.Date) obj[6]).toLocalDate()
                            : null
            );

            dto.setCallDate(
                    obj[7] != null
                            ? ((java.sql.Timestamp) obj[7]).toLocalDateTime()
                            : null
            );

            dto.setIeName(
                    obj[8] != null ? obj[8].toString() : null
            );

            dto.setCmName(
                    obj[9] != null ? obj[9].toString() : null
            );

            dto.setRitesRio(
                    obj[10] != null ? obj[10].toString() : null
            );

            dto.setStatus(
                    obj[11] != null ? obj[11].toString() : null
            );

            return dto;

        }).toList();
    }


    @Override
    public List<IeWiseCallStatusWorkloadSummaryDto> getSleeperIeWiseCallStatusWorkloadSummary(
            String cmEmployeeCode) {

        List<Object[]> results =
                inspectionCallRepository
                        .getSleeperIeWiseCallStatusWorkloadSummary(
                                cmEmployeeCode
                        );

        return results.stream().map(obj -> {

            IeWiseCallStatusWorkloadSummaryDto dto =
                    new IeWiseCallStatusWorkloadSummaryDto();

            dto.setIeId(
                    obj[0] != null ? obj[0].toString() : null
            );

            dto.setIeName(
                    obj[1] != null ? obj[1].toString() : null
            );

            dto.setNoOfCallsPending(
                    obj[2] != null
                            ? ((Number) obj[2]).longValue()
                            : 0L
            );

            dto.setNoOfCallsUnderInspection(
                    obj[3] != null
                            ? ((Number) obj[3]).longValue()
                            : 0L
            );

            dto.setNoOfCallsPendingForIc(
                    obj[4] != null
                            ? ((Number) obj[4]).longValue()
                            : 0L
            );

            dto.setNoOfCallsOverdue(
                    obj[5] != null
                            ? ((Number) obj[5]).longValue()
                            : 0L
            );

            return dto;

        }).toList();
    }


    @Override
    public List<IeOperationalSlaPerformanceSummaryDto> getSleeperIeOperationalSlaPerformanceSummary(String cmEmployeeCode) {

        List<Object[]> result =
                inspectionCallRepository
                        .getSleeperIeOperationalSlaPerformanceSummary(cmEmployeeCode);

        return result.stream().map(obj -> {

            IeOperationalSlaPerformanceSummaryDto dto =
                    new IeOperationalSlaPerformanceSummaryDto();

            dto.setIeId(
                    obj[0] != null ? obj[0].toString() : null
            );

            dto.setIeName(
                    obj[1] != null ? obj[1].toString() : null
            );

            dto.setTotalCalls(
                    obj[2] != null
                            ? ((Number) obj[2]).longValue()
                            : 0L
            );

            dto.setOverdueCallsAttended(
                    obj[3] != null
                            ? ((Number) obj[3]).longValue()
                            : 0L
            );

            dto.setCallsAccepted(
                    obj[4] != null
                            ? ((Number) obj[4]).longValue()
                            : 0L
            );

            dto.setCallsRejected(
                    obj[5] != null
                            ? ((Number) obj[5]).longValue()
                            : 0L
            );

            dto.setCallsPartiallyAcceptedRejected(
                    obj[6] != null
                            ? ((Number) obj[6]).longValue()
                            : 0L
            );

            dto.setIcIssued(
                    obj[7] != null
                            ? ((Number) obj[7]).longValue()
                            : 0L
            );

            return dto;

        }).toList();
    }

    @Override
    public java.util.Map<String, Long> getFinalInspectionCallStatusCounts() {
        List<SleeperWorkflowTransaction> latestTransactions = sleeperWorkflowRepository.findLatestTransactionsForWorkflow2();
        
        long pending = 0;
        long underInspection = 0;
        
        for (SleeperWorkflowTransaction tx : latestTransactions) {
            String jobStatus = tx.getJobStatus();
            if (jobStatus == null || jobStatus.trim().isEmpty()) {
                continue;
            }
            
            String statusUpper = jobStatus.trim().toUpperCase();
            if ("SCHEDULED".equals(statusUpper)) {
                pending++;
            } else if (!"CREATED".equals(statusUpper) 
                    && !"RIO_VERIFIED".equals(statusUpper) 
                    && !"COMPLETED".equals(statusUpper)) {
                underInspection++;
            }
        }
        
        java.util.Map<String, Long> response = new java.util.HashMap<>();
        response.put("pending", pending);
        response.put("underInspection", underInspection);
        return response;
    }
}

