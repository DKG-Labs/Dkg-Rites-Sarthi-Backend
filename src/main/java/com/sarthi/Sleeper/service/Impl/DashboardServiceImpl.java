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
import com.sarthi.util.CommonUtils;
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
    @Autowired
    private com.sarthi.Sleeper.repository.FInalCallRepo.SleeperFinalResultRepository sleeperFinalResultRepository;
    @Autowired
    private com.sarthi.repository.PoHeaderRepository poHeaderRepository;
    @Override
    public Long getRejectedSleepersCount() {
        return demouldingDefectiveSleeperRepository.countByWithReasons();
    }

    private volatile Long totalRejectedCountCache = 0L;
    private volatile Double rejectionPercentageCache = 0.0;
    private volatile boolean isCacheInitialized = false;

    // fixedDelay (not fixedRate): the next run starts 5 min AFTER the previous one finishes, so a slow run can't stack up.
    // initialDelay: don't hit the DB with this heavy query right at startup (getTotalRejectedCount() lazy-loads it anyway).
    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 300000, initialDelay = 120000)
    public void updateDashboardMetrics() {
        try {
            Long rejected = 0L;
            java.util.List<Object[]> finalSummary = sleeperFinalResultRepository.getAllSleeperFinalSummary();
            if (finalSummary != null && !finalSummary.isEmpty() && finalSummary.get(0) != null) {
                Object[] row = finalSummary.get(0);
                long fRejNos = row[2] != null ? ((Number) row[2]).longValue() : 0L;
                long fRejSet = row[3] != null ? ((Number) row[3]).longValue() : 0L;
                rejected = fRejNos + fRejSet;
            }
            if (rejected == 0L) {
                Long fallback = inspectionTestResultRepository.getTotalRejectedCount();
                if (fallback != null && fallback > 0) rejected = fallback;
            }
            Long productionCount = productionDeclarationRepository.getTotalProductionCount();
            Long demouldRejected = demouldingDefectiveSleeperRepository.countByWithReasons();
            Long totalRejected = (demouldRejected != null ? demouldRejected : 0L) + (rejected != null ? rejected : 0L);
            
            totalRejectedCountCache = rejected != null ? rejected : 0L;
            
            if (productionCount == null || productionCount == 0) {
                rejectionPercentageCache = 0.0;
            } else {
                rejectionPercentageCache = (totalRejected * 100.0) / productionCount;
            }
            isCacheInitialized = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Long getTotalRejectedCount() {
        if (!isCacheInitialized) {
            synchronized(this) {
                if (!isCacheInitialized) {
                    updateDashboardMetrics();
                }
            }
        }
        return totalRejectedCountCache;
    }


    @Override
    public Double getRejectionPercentage() {
        if (!isCacheInitialized) {
            synchronized(this) {
                if (!isCacheInitialized) {
                    updateDashboardMetrics();
                }
            }
        }
        return rejectionPercentageCache;
    }

    /** Runs one monthly-analysis query and logs its time; a null result becomes an empty list so one failure doesn't break the page. */
    private static List<Object[]> timedAnalysisQuery(String name, java.util.function.Supplier<List<Object[]>> query) {
        long t0 = System.currentTimeMillis();
        try {
            List<Object[]> rows = query.get();
            return rows != null ? rows : new ArrayList<>();
        } finally {
            System.out.println("[SleeperAnalysis] " + name + " took " + (System.currentTimeMillis() - t0) + " ms");
        }
    }

    @Override
    public List<MonthlyAnalysisDto> getMonthlyAnalysis(String startDate, String endDate) {


        LocalDate sDate = CommonUtils.parseDateFlexible(startDate, LocalDate.now().minusMonths(1));
        LocalDate eDate = CommonUtils.parseDateFlexible(endDate, LocalDate.now());
        LocalDateTime start = sDate.atStartOfDay();
        LocalDateTime end = eDate.atTime(23, 59, 59);


        // The five queries are independent, so they run at the same time (the request now takes as long as the
        // slowest one instead of the sum of all five). Each one logs its own time as "[SleeperAnalysis] ...".
        java.util.concurrent.CompletableFuture<List<Object[]>> cfProd = java.util.concurrent.CompletableFuture.supplyAsync(
                () -> timedAnalysisQuery("production", () -> productionDeclarationRepository.getProduction(start, end)));
        java.util.concurrent.CompletableFuture<List<Object[]>> cfProc = java.util.concurrent.CompletableFuture.supplyAsync(
                () -> timedAnalysisQuery("process rejection", () -> inspectionTestResultRepository.getProcessRejection(start, end)));
        java.util.concurrent.CompletableFuture<List<Object[]>> cfFinal = java.util.concurrent.CompletableFuture.supplyAsync(
                () -> timedAnalysisQuery("final rejection", () -> inspectionTestResultRepository.getFinalRejection(start, end)));
        java.util.concurrent.CompletableFuture<List<Object[]>> cfMaster = java.util.concurrent.CompletableFuture.supplyAsync(
                () -> timedAnalysisQuery("plant master", () -> productionDeclarationRepository.getPlantMasterData()));
        java.util.concurrent.CompletableFuture<List<Object[]>> cfPo = java.util.concurrent.CompletableFuture.supplyAsync(
                () -> timedAnalysisQuery("po details", () -> productionDeclarationRepository.getPoDetailsByPlant()));

        List<Object[]> prodList = cfProd.join();
        List<Object[]> procList = cfProc.join();
        List<Object[]> finalList = cfFinal.join();
        List<Object[]> masterList = cfMaster.join();
        List<Object[]> poList = cfPo.join();
        Map<String, Long> productionMap = new HashMap<>();
        Map<String, Long> processMap = new HashMap<>();
        Map<String, Long> finalMap = new HashMap<>();

        Map<String, String> plantNameMap = new HashMap<>();
        Map<String, String> rioMap = new HashMap<>();

        Map<String, Long> noOfPosMap = new HashMap<>();
        Map<String, Long> poQtyMap = new HashMap<>();
        Map<String, String> uomMap = new HashMap<>();

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
        for (Object[] o : poList) {

            String plantId = (String) o[0];

            noOfPosMap.put(
                    plantId,
                    o[1] != null ? ((Number) o[1]).longValue() : 0L
            );

            poQtyMap.put(
                    plantId,
                    o[2] != null ? ((Number) o[2]).longValue() : 0L
            );

            uomMap.put(
                    plantId,
                    o[3] != null ? o[3].toString() : ""
            );
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

            dto.setNoOfPos(noOfPosMap.getOrDefault(plantId, 0L));
            dto.setPoQty(poQtyMap.getOrDefault(plantId, 0L));
            dto.setUom(uomMap.getOrDefault(plantId, ""));
            result.add(dto);
        }



        return result;
    }


    public List<SleeperPoWiseAnalysisDTO> getPoWiseAnalysis(
            String plantId,
            String startDate,
            String endDate) {

        LocalDate sDate = CommonUtils.parseDateFlexible(startDate, LocalDate.now().minusMonths(1));
        LocalDate eDate = CommonUtils.parseDateFlexible(endDate, LocalDate.now());
        LocalDateTime start = sDate.atStartOfDay();
        LocalDateTime end = eDate.atTime(23, 59, 59);

        List<Object[]> rows =
                productionDeclarationRepository.getPoWiseAnalysis(
                        plantId,start,end);

        List<SleeperPoWiseAnalysisDTO> result = new ArrayList<>();

        for(Object[] row : rows){

            Long production =
                    row[6] == null ? 0L :
                            ((Number)row[6]).longValue();

            Long processRejection =
                    row[7] == null ? 0L :
                            ((Number)row[7]).longValue();

            Long finalRejection =
                    row[8] == null ? 0L :
                            ((Number)row[8]).longValue();

            Long acceptance =
                    production - processRejection - finalRejection;

            SleeperPoWiseAnalysisDTO dto = new SleeperPoWiseAnalysisDTO();

            dto.setRlyZone((String) row[0]);
            dto.setPoNumber((String) row[1]);

            if(row[2] != null){
                if(row[2] instanceof java.sql.Date d){
                    dto.setPoDate(d.toLocalDate());
                } else if(row[2] instanceof java.sql.Timestamp t){
                    dto.setPoDate(t.toLocalDateTime().toLocalDate());
                }
            }

            dto.setPoQty(
                    row[3] == null ? 0L :
                            ((Number)row[3]).longValue());

            dto.setPlantName((String) row[4]);
            dto.setInspectedBy((String) row[5]);

            dto.setProduction(production);
            dto.setProcessRejection(processRejection);
            dto.setFinalRejection(finalRejection);
            dto.setAcceptance(acceptance);

            dto.setRejectionPercentage(
                    production == 0 ? 0 :
                            ((processRejection + finalRejection) * 100.0) / production
            );

            dto.setNoOfPos(
                    row[9] == null ? 0L :
                            ((Number)row[9]).longValue());

            dto.setUom(row[10] == null ? "" : row[10].toString());

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

        // One row per PO and unit of measurement
        List<com.sarthi.Sleeper.dto.SleeperDashboardDtos.MprUomProjection> data =
                inspectionCallRepository.getMprDataByUom(startDate, endDate);

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
                    d.getUom(),
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

/*
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
    }  */

    @Override
    public DefectDistributionResponseDto getDefectReasonDistribution(
            LocalDate fromDate,
            LocalDate toDate) {

        List<Long> pdIds = closedIcPdIds(fromDate, toDate);
        List<Object[]> rows = pdIds.isEmpty()
                ? new ArrayList<>()
                : inspectionTestHeaderRepository.getDefectReasonDistributionsByPdIds(pdIds);

        Map<String, Long> groupedDefects = new LinkedHashMap<>();

        for (Object[] row : rows) {

            String category = (String) row[0];
            String defectReason = row[1] != null
                    ? row[1].toString().toLowerCase()
                    : "";

            Long defectCount = ((Number) row[2]).longValue();

            //  Demoulding Rejection
            if ("Visual (Demoulding)".equals(category)
                    || "Dimension (Demoulding)".equals(category)) {

                groupedDefects.merge(
                        "Demoulding Rejection",
                        defectCount,
                        Long::sum
                );
                continue;
            }

            //  Visual Defects
            if (defectReason.contains("surface")
                    || defectReason.contains("honeycomb")
                    || defectReason.contains("crack")
                    || defectReason.contains("insert missing")
                    || defectReason.contains("tilt")
                    || defectReason.contains("sink")
                    || defectReason.contains("dowel")) {

                groupedDefects.merge(
                        "Visual Defects",
                        defectCount,
                        Long::sum
                );
                continue;
            }

            //  General Dimensional Defect
            if (defectReason.contains("outer gauge")) {

                groupedDefects.merge(
                        "General Dimensional Defect",
                        defectCount,
                        Long::sum
                );
                continue;
            }

            //  Critical Dimensions
            if ("Final (Critical)".equals(category)) {

                groupedDefects.merge(
                        "Critical Dimensions",
                        defectCount,
                        Long::sum
                );
                continue;
            }

            //  Non Critical
            if ("Final (Non-Critical)".equals(category)) {

                groupedDefects.merge(
                        "Non Critical",
                        defectCount,
                        Long::sum
                );
            }
        }

        long total = groupedDefects.values()
                .stream()
                .mapToLong(Long::longValue)
                .sum();

        List<DefectReasonDistributionDto> list =
                new ArrayList<>();

        for (Map.Entry<String, Long> entry : groupedDefects.entrySet()) {

            double percentage = total == 0
                    ? 0
                    : (entry.getValue() * 100.0) / total;

            list.add(
                    new DefectReasonDistributionDto(
                            entry.getKey(),      // category
                            null,                // defectReason not needed
                            entry.getValue(),    // count
                            Math.round(percentage * 100.0) / 100.0
                    )
            );
        }

        DefectDistributionResponseDto response =
                new DefectDistributionResponseDto();

        response.setDefects(list);

        return response;
    }

  /*  @Override
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
    }  */

    /** Production declarations of the batches in closed ICs (latest status SEND_CALL_TO_IBS, IC date inside the range). */
    private List<Long> closedIcPdIds(LocalDate fromDate, LocalDate toDate) {
        // Batches of the closed ICs (latest status SEND_CALL_TO_IBS, IC date inside the range)
        List<Object[]> closedBatches = inspectionTestHeaderRepository.getClosedIcBatches(fromDate, toDate);

        java.util.function.Function<Object, String> clean = o -> o == null ? "" : o.toString().trim().toLowerCase();
        java.util.function.Function<Object, String> plantKey =
                o -> o == null ? "" : o.toString().replace(":", "").trim().toLowerCase();

        java.util.Set<String> batchNos = new java.util.HashSet<>();
        java.util.Set<String> wanted = new java.util.HashSet<>();   // batch|casting date|plant
        for (Object[] cb : closedBatches) {
            if (cb[0] == null) continue;
            batchNos.add(cb[0].toString());
            batchNos.add(cb[0].toString().trim());
            String castDay = String.valueOf(cb[1]).trim();
            if (castDay.length() > 10) castDay = castDay.substring(0, 10);
            wanted.add(clean.apply(cb[0]) + "|" + castDay + "|" + plantKey.apply(cb[2]));
        }

        // 2. The production declarations of those batches
        List<Long> pdIds = new ArrayList<>();
        if (!batchNos.isEmpty()) {
            for (com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionDeclaration pd
                    : productionDeclarationRepository.findAllByTrimmedBatchNumbers(batchNos)) {
                String key = clean.apply(pd.getBatchNumber()) + "|"
                        + (pd.getCastingDate() == null ? "null" : pd.getCastingDate().toString())
                        + "|" + plantKey.apply(pd.getPlantId());
                if (wanted.contains(key)) pdIds.add(pd.getId());
            }
        }

        return pdIds;
    }

    @Override
    public ParetoAnalysisResponseDto getParetoAnalysis(
            LocalDate fromDate,
            LocalDate toDate) {

        List<Long> pdIds = closedIcPdIds(fromDate, toDate);

        // 3. Rejection reasons of those batches
        List<Object[]> rows = pdIds.isEmpty()
                ? new ArrayList<>()
                : inspectionTestHeaderRepository.getParetoAnalysis(pdIds);

        Map<String, Long> groupedDefects =
                new LinkedHashMap<>();
        Map<String, String> displayName = new HashMap<>();

        for (Object[] row : rows) {

            String reason = row[0] == null
                    ? ""
                    : row[0].toString().toLowerCase();

            Long count =
                    ((Number) row[1]).longValue();

            // A reason that matches none of the groups below keeps its own name instead of going into "Others"
            String rawReason = row[0] == null ? "" : row[0].toString().trim();
            String category = rawReason.isEmpty() ? "Unspecified" : rawReason;

            if (reason.contains("surface defect")
                    || reason.contains("surface damage")) {

                category = "Surface Defect";
            }
            else if (reason.contains("honeycomb")) {

                category = "Honeycomb";
            }
            else if (reason.contains("crack")) {

                category = "Crack";
            }
            else if (reason.contains("insert missing")
                    || reason.contains("tilt")
                    || reason.contains("sink")) {

                category = "Insert Missing / Tilt / Sink";
            }
            else if (reason.contains("dowel missing")) {

                category = "Dowel Missing / Tilt / Sink";
            }
            else if (reason.contains("outer gauge")) {

                category = "Outer Gauge";
            }
            else if (reason.contains("rail seat") && !reason.contains("slope")) {

                category = "Rail Seat";
            }
            else if (reason.contains("toegap")
                    || reason.contains("toe gap")) {

                category = "Toe Gap";
            }
            else if (reason.contains("slope gauge")
                    || reason.contains("rail seat slope")) {

                category = "Rail Seat Slope";
            }
            else if (reason.contains("position error")) {

                category = "Location of Inserts in Turn Outs";
            }
            else if (reason.contains("distance error")) {

                category = "Angularity in Inserts of Turn Outs";
            }
            else if (reason.contains("location of dowel")) {

                category = "Location of Dowel";
            }
            else if (reason.contains("height gauge")) {

                category = "Height Gauge";
            }
            else if (reason.contains("wind gauge")) {

                category = "Wind Gauge";
            }
            else if (reason.contains("length")) {

                category = "Length of Sleeper";
            }
            else if (reason.contains("width")) {

                category = "Width of Sleeper";
            }
            else if (reason.contains("camber")) {

                category = "Camber";
            }
            else if (reason.contains("hts")) {

                category = "HTS Wire Position";
            }
            else if (reason.contains("water cube")) {

                category = "Water Cube Strength";
            }
            else if (reason.contains("steam cube")) {

                category = "Steam Cube Strength";
            }
            else if (reason.contains("moment of resistance")) {

                category = "Moment of Resistance";
            }
            else if (reason.contains("moment of failure")) {

                category = "Moment of Failure";
            }
            else if (reason.contains("modulus of rupture")) {

                category = "Modulus of Rupture";
            }
            else if (reason.contains("nftc")
                    || reason.contains("ftc")) {

                category = "FTC";
            }

            // Reasons that differ only in letter case are one bar
            final String finalCategory = category;
            groupedDefects.merge(
                    displayName.computeIfAbsent(finalCategory.toLowerCase(), k -> finalCategory),
                    count,
                    Long::sum
            );
        }

        long total = groupedDefects.values()
                .stream()
                .mapToLong(Long::longValue)
                .sum();

        List<ParetoAnalysisDto> list =
                new ArrayList<>();

        double cumulative = 0.0;

        for (Map.Entry<String, Long> entry
                : groupedDefects.entrySet()) {

            double percentage =
                    total == 0
                            ? 0
                            : (entry.getValue() * 100.0) / total;

            cumulative += percentage;

            list.add(
                    new ParetoAnalysisDto(
                            entry.getKey(),
                            entry.getValue(),
                            Math.round(percentage * 100.0) / 100.0,
                            Math.round(cumulative * 100.0) / 100.0
                    )
            );
        }

        list.sort(
                Comparator.comparing(
                        ParetoAnalysisDto::getDefectCount
                ).reversed()
        );

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
        System.out.println("rows" );

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
        return getVendorPlantCompanyNames(null);
    }

    @Override
    public List<String> getVendorPlantCompanyNames(String zone) {
        if (zone != null && !zone.isBlank()) {
            return vendorPlantRepository.findDistinctCompanyNamesByZone(zone.trim());
        }
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
            String action = tx.getAction();
            String status = tx.getStatus();
            String statusUpper = status != null ? status.trim().toUpperCase() : "";
            String actionUpper = action != null ? action.trim().toUpperCase() : "";
            String jobStatusUpper = jobStatus != null ? jobStatus.trim().toUpperCase() : "";
            
            if ("IC_GENERATION".equals(jobStatusUpper) || "IC_GENERATION".equals(actionUpper) || "IC_GENERATION".equals(statusUpper)
                    || "SEND_CALL_TO_IBS".equals(statusUpper) || "SEND_CALL_TO_IBS".equals(actionUpper) || "SEND_CALL_TO_IBS".equals(jobStatusUpper)
                    || "SENT_TO_IBS".equals(statusUpper) || "SENT_TO_IBS".equals(actionUpper) || "SENT_TO_IBS".equals(jobStatusUpper)
                    || "CLOSED".equals(statusUpper) || "CLOSED".equals(actionUpper) || "CLOSED".equals(jobStatusUpper)
                    || "COMPLETED".equals(statusUpper) || "COMPLETED".equals(actionUpper) || "COMPLETED".equals(jobStatusUpper)
                    || jobStatusUpper.contains("CANCEL") || actionUpper.contains("CANCEL") || statusUpper.contains("CANCEL")) {
                continue;
            }
            
            if ("INITIATED".equals(jobStatusUpper) || "PO_VERIFICATION".equals(jobStatusUpper) || "PAUSED".equals(jobStatusUpper) || "WITHHELD".equals(jobStatusUpper)
                    || "INITIATE_CALL".equals(actionUpper) || "PO_VERIFICATION".equals(actionUpper) || "PAUSE".equals(actionUpper) || "WITHHELD".equals(actionUpper)
                    || "INITIATED".equals(statusUpper) || "PO_VERIFICATION".equals(statusUpper) || "PAUSED".equals(statusUpper) || "WITHHELD".equals(statusUpper)) {
                underInspection++;
            } else {
                pending++;
            }
        }
        
        java.util.Map<String, Long> response = new java.util.HashMap<>();
        response.put("pending", pending);
        response.put("underInspection", underInspection);
        return response;
    }


        /** One report row: a plant and a sleeper category. */
        private static class QualityRow {
            String plantKey = "";
            String rawPlantId = "";
            String category = "Mainline";
            long produced, inspected, icQty, rejectedInProcess;
            long dimension, endDamage, honeycomb, dowel, other;
            final java.util.Set<String> calls = new java.util.HashSet<>();
            String lastIcDay = "";
        }

        private static long qNum(Object o) {
            return o instanceof Number ? ((Number) o).longValue() : 0L;
        }

        /** Runs one query of the quality report and logs how long it took; a failure is passed on, not hidden. */
        private static <T> T timedQualityQuery(String name, java.util.function.Supplier<T> query) {
            long t0 = System.currentTimeMillis();
            try {
                return query.get();
            } finally {
                System.out.println("[QualityReport] " + name + " took " + (System.currentTimeMillis() - t0) + " ms");
            }
        }

        @Override
        public List<QualitySleeperReportDto> getQualitySleeperReport(
                String startDate,
                String endDate,
                String zone) {

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

            java.util.function.Function<Object, String> norm = o -> o == null ? "" : o.toString().trim().toLowerCase();
            java.util.function.Function<Object, String> plantKey =
                    o -> o == null ? "" : o.toString().replace(":", "").trim().toLowerCase();
            java.util.function.Function<Object, String> day = o -> {
                String s = o == null ? "" : String.valueOf(o).trim();
                return s.length() > 10 ? s.substring(0, 10) : s;
            };
            java.util.function.Function<Object, String> category = o -> {
                String s = o == null ? "" : o.toString().trim();
                return s.isEmpty() ? "Mainline" : s;
            };

            // The queries that do not depend on the closed ICs start now and run alongside them
            java.util.concurrent.CompletableFuture<List<Object[]>> cfPlants =
                    java.util.concurrent.CompletableFuture.supplyAsync(() -> timedQualityQuery("plant zones",
                            () -> inspectionTestHeaderRepository.getPlantZones()));
            java.util.concurrent.CompletableFuture<List<Object[]>> cfProduced =
                    java.util.concurrent.CompletableFuture.supplyAsync(() -> timedQualityQuery("produced",
                            () -> inspectionTestHeaderRepository.getProducedByPlantCategory(start, end)));

            // Plants: railway zone and the name to show for the CSP
            java.util.Map<String, String[]> plants = new java.util.HashMap<>();   // zone, plant name, company, raw id
            for (Object[] r : cfPlants.join()) {
                String k = plantKey.apply(r[0]);
                if (k.isEmpty()) continue;
                String[] cur = plants.computeIfAbsent(k, x -> new String[] { "", "", "", r[0].toString().trim() });
                if (cur[0].isBlank() && r[1] != null) cur[0] = r[1].toString().trim();
                if (cur[1].isBlank() && r[2] != null) cur[1] = r[2].toString().trim();
                if (cur[2].isBlank() && r[3] != null) cur[2] = r[3].toString().trim();
            }

            java.util.Map<String, QualityRow> rows = new java.util.HashMap<>();
            java.util.function.BiFunction<String, String, QualityRow> rowOf = (rawPlant, cat) -> {
                String pk = plantKey.apply(rawPlant);
                return rows.computeIfAbsent(pk + "|" + cat.toLowerCase(), x -> {
                    QualityRow q = new QualityRow();
                    q.plantKey = pk;
                    q.rawPlantId = rawPlant == null ? "" : rawPlant.trim();
                    q.category = cat;
                    return q;
                });
            };

            // 1. Closed ICs (latest status SEND_CALL_TO_IBS, IC date inside the range)
            List<Object[]> closed = timedQualityQuery("closed ICs",
                    () -> inspectionTestHeaderRepository.getClosedIcBatchRows(start, end));

            java.util.Set<String> batchNos = new java.util.HashSet<>();
            java.util.Set<String> callNos = new java.util.HashSet<>();
            for (Object[] r : closed) {
                if (r[0] != null) callNos.add(r[0].toString().trim());
                if (r[1] == null) continue;
                batchNos.add(r[1].toString());
                batchNos.add(r[1].toString().trim());
            }

            // These only need the batch / call numbers, so they run while the declarations are looked up
            java.util.concurrent.CompletableFuture<List<Object[]>> cfDemoulding = batchNos.isEmpty()
                    ? java.util.concurrent.CompletableFuture.completedFuture(new ArrayList<Object[]>())
                    : java.util.concurrent.CompletableFuture.supplyAsync(() -> timedQualityQuery("demoulding",
                            () -> inspectionTestHeaderRepository.getDemouldingRejectionsByBatch(batchNos)));
            java.util.concurrent.CompletableFuture<List<Object[]>> cfFinalResult = callNos.isEmpty()
                    ? java.util.concurrent.CompletableFuture.completedFuture(new ArrayList<Object[]>())
                    : java.util.concurrent.CompletableFuture.supplyAsync(() -> timedQualityQuery("final results",
                            () -> inspectionTestHeaderRepository.getFinalAcceptedByCalls(callNos)));
            java.util.concurrent.CompletableFuture<List<Object[]>> cfRejGroups = callNos.isEmpty()
                    ? java.util.concurrent.CompletableFuture.completedFuture(new ArrayList<Object[]>())
                    : java.util.concurrent.CompletableFuture.supplyAsync(() -> timedQualityQuery("rejection groups",
                            () -> inspectionTestHeaderRepository.getFinalRejectionGroupsByCalls(callNos)));

            // Their production declarations (batch number + casting date + plant)
            java.util.Map<String, Long> pdByKey = new java.util.HashMap<>();
            if (!batchNos.isEmpty()) {
                for (com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionDeclaration pd
                        : timedQualityQuery("declarations",
                                () -> productionDeclarationRepository.findAllByTrimmedBatchNumbers(batchNos))) {
                    String key = norm.apply(pd.getBatchNumber()) + "|"
                            + (pd.getCastingDate() == null ? "" : pd.getCastingDate().toString())
                            + "|" + plantKey.apply(pd.getPlantId());
                    pdByKey.putIfAbsent(key, pd.getId());
                    pdByKey.putIfAbsent(norm.apply(pd.getBatchNumber()) + "|" + plantKey.apply(pd.getPlantId()), pd.getId());
                }
            }
            java.util.List<Long> pdIds = new ArrayList<>(new java.util.HashSet<>(pdByKey.values()));

            java.util.Map<Long, String> categoryByPd = new java.util.HashMap<>();
            java.util.Map<Long, long[]> defectsByPd = new java.util.HashMap<>();
            if (!pdIds.isEmpty()) {
                java.util.concurrent.CompletableFuture<List<Object[]>> cfCategory =
                        java.util.concurrent.CompletableFuture.supplyAsync(() -> timedQualityQuery("categories",
                                () -> inspectionTestHeaderRepository.getSleeperCategoryByPdIds(pdIds)));
                java.util.concurrent.CompletableFuture<List<Object[]>> cfDefects =
                        java.util.concurrent.CompletableFuture.supplyAsync(() -> timedQualityQuery("defect counts",
                                () -> inspectionTestHeaderRepository.getFinalDefectCountsByPdIds(pdIds)));
                for (Object[] r : cfCategory.join()) {
                    categoryByPd.put(qNum(r[0]), category.apply(r[1]));
                }
                for (Object[] r : cfDefects.join()) {
                    defectsByPd.put(qNum(r[0]), new long[] { qNum(r[1]), qNum(r[2]), qNum(r[3]), qNum(r[4]), qNum(r[5]) });
                }
            }

            java.util.Map<String, Long> demouldingByBatch = new java.util.HashMap<>();
            for (Object[] r : cfDemoulding.join()) {
                demouldingByBatch.merge(norm.apply(r[0]) + "|" + day.apply(r[1]) + "|" + plantKey.apply(r[2]),
                        qNum(r[3]), Long::sum);
                demouldingByBatch.merge(norm.apply(r[0]) + "|" + plantKey.apply(r[2]), qNum(r[3]), Long::sum);
            }

            // The accepted quantity of each IC as the IC itself states it (numbers, or sets for turnouts); this is the
            // figure the dashboard and the monthly progress report use. Per-batch figures are only the fallback.
            java.util.Map<String, long[]> finalByCall = new java.util.HashMap<>();
            // acc_nos, acc_sets, off_nos, off_sets, rej_nos, rej_sets
            for (Object[] r : cfFinalResult.join()) {
                finalByCall.put(r[0].toString().trim(),
                        new long[] { qNum(r[1]), qNum(r[2]), qNum(r[3]), qNum(r[4]), qNum(r[5]), qNum(r[6]) });
            }
            // The IC's rejected sleepers by defect group (dimension, end damage, honeycomb/surface/crack, dowel, other)
            java.util.Map<String, long[]> rejectionGroupsByCall = new java.util.HashMap<>();
            for (Object[] r : cfRejGroups.join()) {
                rejectionGroupsByCall.put(r[0].toString().trim(),
                        new long[] { qNum(r[1]), qNum(r[2]), qNum(r[3]), qNum(r[4]), qNum(r[5]) });
            }

            // 2. Offered quantity, accepted quantity, calls and last IC date per plant and category;
            //    rejections are counted once per batch
            java.util.Set<String> countedBatch = new java.util.HashSet<>();
            java.util.Set<String> countedPd = new java.util.HashSet<>();
            java.util.Set<String> countedCall = new java.util.HashSet<>();

            // A call's batches can fall under more than one plant / sleeper category row. The IC is still one IC, so it
            // is counted in a single "home" row: the one holding most of the call's offered quantity.
            java.util.Map<String, java.util.Map<String, Long>> offeredByCallRow = new java.util.HashMap<>();
            for (Object[] r : closed) {
                Long homePd = pdByKey.get(norm.apply(r[1]) + "|" + day.apply(r[2]) + "|" + plantKey.apply(r[3]));
                if (homePd == null) homePd = pdByKey.get(norm.apply(r[1]) + "|" + plantKey.apply(r[3]));
                String homeCat = homePd != null ? categoryByPd.getOrDefault(homePd, "Mainline") : "Mainline";
                String homeCall = r[0] == null ? "" : r[0].toString().trim();
                offeredByCallRow.computeIfAbsent(homeCall, c -> new java.util.HashMap<>())
                        .merge(plantKey.apply(r[3]) + "|" + homeCat.toLowerCase(), qNum(r[4]), Long::sum);
            }
            java.util.Map<String, String> homeRowByCall = new java.util.HashMap<>();
            offeredByCallRow.forEach((c, byRow) -> homeRowByCall.put(c, byRow.entrySet().stream()
                    .max(java.util.Map.Entry.<String, Long>comparingByValue().thenComparing(java.util.Map.Entry::getKey))
                    .get().getKey()));

            for (Object[] r : closed) {
                String key = norm.apply(r[1]) + "|" + day.apply(r[2]) + "|" + plantKey.apply(r[3]);
                String looseKey = norm.apply(r[1]) + "|" + plantKey.apply(r[3]);
                Long pdId = pdByKey.get(key);
                if (pdId == null) pdId = pdByKey.get(looseKey);
                String cat = pdId != null ? categoryByPd.getOrDefault(pdId, "Mainline") : "Mainline";
                QualityRow q = rowOf.apply(r[3] == null ? null : r[3].toString(), cat);
                String rowKey = q.plantKey + "|" + cat.toLowerCase();

                String callNo = r[0] == null ? "" : r[0].toString().trim();
                long[] fin = finalByCall.get(callNo);
                boolean homeRow = rowKey.equals(homeRowByCall.get(callNo));
                if (fin == null) {
                    // No final result for this call: fall back to this batch's own figures
                    q.inspected += qNum(r[4]);
                    q.icQty += qNum(r[5]);
                } else if (homeRow && countedCall.add(callNo)) {
                    // The IC's figures belong to the call, so they are added once (in its home row), not once per
                    // batch or row: offered, accepted (sets for turnouts) and rejected, as printed on the certificate
                    q.inspected += fin[2];
                    q.icQty += cat.toLowerCase().contains("turnout") && fin[1] > 0 ? fin[1] : fin[0];

                    long[] g = rejectionGroupsByCall.getOrDefault(callNo, new long[5]);
                    long grouped = g[0] + g[1] + g[2] + g[3] + g[4];
                    q.dimension += g[0];
                    q.endDamage += g[1];
                    q.honeycomb += g[2];
                    q.dowel += g[3];
                    // Rejected sleepers the reason list does not cover still count, so the groups add up to the IC total
                    q.other += g[4] + Math.max(0L, fin[4] - grouped);
                }
                if (r[0] != null && homeRow) q.calls.add(callNo);
                String icDay = day.apply(r[6]);
                if (icDay.compareTo(q.lastIcDay) > 0) q.lastIcDay = icDay;

                if (countedBatch.add(rowKey + "|" + key)) {
                    q.rejectedInProcess += demouldingByBatch.containsKey(key)
                            ? demouldingByBatch.get(key) : demouldingByBatch.getOrDefault(looseKey, 0L);
                }
                // Defect groups of the batch's inspection results: only a fallback for calls without a final result
                if (fin == null && pdId != null && countedPd.add(rowKey + "|" + pdId)) {
                    long[] d = defectsByPd.get(pdId);
                    if (d != null) {
                        q.dimension += d[0];
                        q.endDamage += d[1];
                        q.honeycomb += d[2];
                        q.dowel += d[3];
                        q.other += d[4];
                    }
                }
            }

            // 3. Sleepers produced: verified declarations cast inside the range
            for (Object[] r : cfProduced.join()) {
                QualityRow q = rowOf.apply(r[0] == null ? null : r[0].toString(), category.apply(r[1]));
                q.produced += qNum(r[2]);
            }

            // 4. Build the report rows, filtered by the selected zone
            boolean zoneFilter = zone != null && !zone.isBlank() && !"all".equalsIgnoreCase(zone.trim());
            java.time.format.DateTimeFormatter shown = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            List<QualitySleeperReportDto> result = new ArrayList<>();
            for (QualityRow q : rows.values()) {
                String[] plant = plants.get(q.plantKey);
                String railway = plant != null && !plant[0].isBlank() ? plant[0] : "Unmapped";
                if (zoneFilter && !railway.equalsIgnoreCase(zone.trim())) continue;

                // The part after "/" is the plant name; when it is only a number, use the plant / company name
                String raw = plant != null ? plant[3] : q.rawPlantId;
                String suffix = raw.contains("/") ? raw.substring(raw.lastIndexOf('/') + 1).trim() : raw.trim();
                String cspName = suffix;
                if (suffix.isEmpty() || suffix.matches("\\d+")) {
                    String named = plant != null ? (!plant[1].isBlank() ? plant[1] : plant[2]) : "";
                    if (!named.isBlank()) cspName = named;
                }

                long totalDefects = q.dimension + q.endDamage + q.honeycomb + q.dowel + q.other;
                QualitySleeperReportDto dto = new QualitySleeperReportDto();
                dto.setRailwayZone(railway);
                dto.setPlantId(raw);
                dto.setCspName(cspName);
                dto.setSleeperType(q.category);
                dto.setTotalProducedSleepers(q.produced);
                dto.setNoOfSleeperInspectedInProcess(q.inspected);
                dto.setNoOfSleeperRejectedInProcess(q.rejectedInProcess);
                dto.setForDimensionToeGauge(q.dimension);
                dto.setForEndDamage(q.endDamage);
                dto.setHoneyCombingSurfaceDefectCrack(q.honeycomb);
                dto.setMissingDowel(q.dowel);
                dto.setOtherDefectsInsertSinkTilt(q.other);
                dto.setTotalRejectedDefects(totalDefects);
                dto.setRejectionPercentage(q.produced > 0 ? Math.round(totalDefects * 10000.0 / q.produced) / 100.0 : 0.0);
                dto.setIcIssuedQty(q.icQty);
                dto.setIcIssuedCount((long) q.calls.size());
                dto.setLastIcDate(q.lastIcDay.isEmpty() ? "-" : LocalDate.parse(q.lastIcDay).format(shown));
                result.add(dto);
            }
            result.sort(java.util.Comparator
                    .comparing(QualitySleeperReportDto::getRailwayZone, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(QualitySleeperReportDto::getCspName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(QualitySleeperReportDto::getSleeperType, String.CASE_INSENSITIVE_ORDER));
            return result;
        }



    private QualitySleeperReportDto mapToDto(
            QualitySleeperReportProjection p){

        QualitySleeperReportDto dto =
                new QualitySleeperReportDto();

        dto.setRailwayZone(
                p.getRailwayZone());

        dto.setPlantId(
                p.getPlantId());

        dto.setSleeperType(
                p.getSleeperType());

        dto.setTotalProducedSleepers(
                p.getTotalProducedSleepers());

        dto.setNoOfSleeperInspectedInProcess(
                p.getNoOfSleeperInspectedInProcess());

        dto.setNoOfSleeperRejectedInProcess(
                p.getNoOfSleeperRejectedInProcess());


        // =====================================
        // DEFECT COUNTS
        // =====================================

        dto.setForDimensionToeGauge(
                p.getForDimensionToeGauge());

        dto.setForEndDamage(
                p.getForEndDamage());

        dto.setHoneyCombingSurfaceDefectCrack(
                p.getHoneyCombingSurfaceDefectCrack());

        dto.setMissingDowel(
                p.getMissingDowel());

        dto.setOtherDefectsInsertSinkTilt(
                p.getOtherDefectsInsertSinkTilt());

        dto.setTotalRejectedDefects(
                p.getTotalRejectedDefects());

        dto.setRejectionPercentage(
                p.getRejectionPercentage());

        return dto;
    }


    public SleeperIcProjection getSleeperIcData(String callNo) {
        return inspectionCallRepository.getSleeperIcData(callNo);
    }

    /** Runs one summary query and logs how long it took; a failing query gives null so the others still count. */
    private static <T> T timedSummaryQuery(String name, java.util.function.Supplier<T> query) {
        long t0 = System.currentTimeMillis();
        try {
            return query.get();
        } catch (Exception ex) {
            System.err.println("[SleeperSummary] " + name + " failed: " + ex.getMessage());
            return null;
        } finally {
            System.out.println("[SleeperSummary] " + name + " took " + (System.currentTimeMillis() - t0) + " ms");
        }
    }

    @Override
    public java.util.Map<String, Object> getSleeperDashboardSummary(String vendor, String zone, String startDate, String endDate) {
        boolean hasVendor = vendor != null && !vendor.isBlank() && !"all".equalsIgnoreCase(vendor.trim());
        boolean hasZone = zone != null && !zone.isBlank() && !"all".equalsIgnoreCase(zone.trim());
        boolean hasStartDate = startDate != null && !startDate.isBlank();
        boolean hasEndDate = endDate != null && !endDate.isBlank();
        boolean filtered = hasVendor || hasZone || hasStartDate || hasEndDate;

        long rejectedInProcess = 0L;
        long rejectedInFinal = 0L;
        double rejectionPercentage = 0.0;
        long pending = 0;
        long underInspection = 0;
        long totalProduction = 0;

        long sleeperIcIssued = 0;

        long finalAcceptedNos = 0L;
        long finalAcceptedSet = 0L;
        long finalRejectedNos = 0L;
        long finalRejectedSet = 0L;
        long icDays = 0L;                       // distinct IC dates inside the range
        long sleeperAvgProductionPerDay = 0L;

        String sDate = hasStartDate ? startDate.trim() : null;
        String eDate = hasEndDate ? endDate.trim() : null;

        java.time.LocalDateTime startLdt = null;
        java.time.LocalDateTime endLdt = null;
        if (sDate != null) {
            try {
                startLdt = java.time.LocalDate.parse(sDate).atStartOfDay();
            } catch (Exception ignored) {}
        }
        if (eDate != null) {
            try {
                endLdt = java.time.LocalDate.parse(eDate).atTime(java.time.LocalTime.MAX);
            } catch (Exception ignored) {}
        }

        if (filtered) {
            String v = hasVendor ? vendor.replace("+", " ").trim() : null;
            if (v != null) {
                String cleanV = v.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
                if (cleanV.contains("BGSLEEPER")) {
                    v = "BG SLEEPER";
                }
            }
            String z = hasZone ? zone.trim() : null;

            long totalOfferedNos = 0L;
            long totalOfferedSet = 0L;

            // The four independent queries run at the same time instead of one after the other
            final String fv = v;
            final String fz = z;
            final String fsDate = sDate;
            final String feDate = eDate;
            final java.time.LocalDateTime fStart = startLdt;
            final java.time.LocalDateTime fEnd = endLdt;
            java.util.concurrent.CompletableFuture<List<Object[]>> cfFinal = java.util.concurrent.CompletableFuture.supplyAsync(
                    () -> timedSummaryQuery("final accepted", () -> sleeperFinalResultRepository.getSleeperFinalSummaryFiltered(fv, fz, fsDate, feDate)));
            java.util.concurrent.CompletableFuture<Long> cfIc = java.util.concurrent.CompletableFuture.supplyAsync(
                    () -> timedSummaryQuery("ic issued", () -> (fv == null && fz == null)
                            ? sleeperWorkflowRepository.countSleeperIcIssuedByDate(fStart, fEnd)
                            : sleeperWorkflowRepository.countSleeperIcIssuedFiltered(fv, fz, fStart, fEnd)));
            java.util.concurrent.CompletableFuture<Long> cfDemould = java.util.concurrent.CompletableFuture.supplyAsync(
                    () -> timedSummaryQuery("process rejected", () -> demouldingDefectiveSleeperRepository.countDemouldingDefectsForIssuedIcsFiltered(fv, fz, fsDate, feDate)));
            java.util.concurrent.CompletableFuture<List<Object[]>> cfStatus = java.util.concurrent.CompletableFuture.supplyAsync(
                    () -> timedSummaryQuery("call status", () -> (fv == null && fz == null)
                            // counts only: [underInspection, pending]; skips the heavy vendor/po/user joins
                            ? sleeperWorkflowRepository.countSleeperInspectionCallStatus(fStart, fEnd)
                            // vendor / zone set: still counts only, with just the joins those filters need
                            : sleeperWorkflowRepository.countSleeperInspectionCallStatusFiltered(fv, fz, fStart, fEnd)));
            final boolean leanStatus = true;

            // 1. Direct Final Summary calculation (checking PO zone, vendor & date)
            try {
                List<Object[]> finalSummaryRows = cfFinal.join();
                if (finalSummaryRows != null && !finalSummaryRows.isEmpty() && finalSummaryRows.get(0) != null) {
                    Object[] row = finalSummaryRows.get(0);
                    if (row.length > 6 && row[6] != null) icDays = ((Number) row[6]).longValue();
                    finalAcceptedNos = row[0] != null ? ((Number) row[0]).longValue() : 0L;
                    finalAcceptedSet = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                    finalRejectedNos = row[2] != null ? ((Number) row[2]).longValue() : 0L;
                    finalRejectedSet = row[3] != null ? ((Number) row[3]).longValue() : 0L;
                    if (row.length > 4 && row[4] != null) totalOfferedNos = ((Number) row[4]).longValue();
                    if (row.length > 5 && row[5] != null) totalOfferedSet = ((Number) row[5]).longValue();
                }
            } catch (Exception ex) {
                System.err.println("Error in getSleeperFinalSummaryFiltered: " + ex.getMessage());
                ex.printStackTrace();
            }
            rejectedInFinal = finalRejectedNos + finalRejectedSet;

            // 2. Direct IC Issued Count (checking PO zone, vendor & date)
            try {
                Long icCount = cfIc.join();
                sleeperIcIssued = icCount != null ? icCount : 0L;
            } catch (Exception ex) {
                System.err.println("Error in countSleeperIcIssuedFiltered: " + ex.getMessage());
                ex.printStackTrace();
            }

            // 3. Process and Production derived from Issued ICs
            try {
                Long demouldDefects = cfDemould.join();
                rejectedInProcess = demouldDefects != null ? demouldDefects : 0L;
            } catch (Exception ex) {
                System.err.println("Error in countDemouldingDefectsForIssuedIcsFiltered: " + ex.getMessage());
                rejectedInProcess = 0L;
            }

            if (totalOfferedNos > 0 || totalOfferedSet > 0) {
                totalProduction = totalOfferedNos + totalOfferedSet + rejectedInProcess;
            } else if (finalAcceptedNos > 0 || finalAcceptedSet > 0 || rejectedInFinal > 0) {
                totalProduction = finalAcceptedNos + finalAcceptedSet + rejectedInFinal + rejectedInProcess;
            } else {
                totalProduction = 0L;
            }

            long totalRejected = rejectedInProcess + rejectedInFinal;
            rejectionPercentage = totalProduction > 0 ? (totalRejected * 100.0) / totalProduction : 0.0;

            // Average production per day = production of the ICs in the range / days on which ICs were issued
            sleeperAvgProductionPerDay = (totalProduction > 0 && icDays > 0)
                    ? Math.round((double) totalProduction / icDays) : 0L;

            // 4. Inspection Call Status (Pending, Under Inspection)
            try {
                List<Object[]> callStatusRows = cfStatus.join();
                if (callStatusRows != null && leanStatus) {
                    if (!callStatusRows.isEmpty() && callStatusRows.get(0) != null) {
                        Object[] r = callStatusRows.get(0);
                        underInspection = r.length > 0 && r[0] != null ? ((Number) r[0]).longValue() : 0L;
                        pending = r.length > 1 && r[1] != null ? ((Number) r[1]).longValue() : 0L;
                    }
                } else if (callStatusRows != null) {
                    for (Object[] callRow : callStatusRows) {
                        String mainStatus = callRow.length > 7 && callRow[7] != null ? callRow[7].toString().trim() : "";
                        String status = callRow.length > 6 && callRow[6] != null ? callRow[6].toString().trim() : "";
                        String effectiveStatus = !mainStatus.isEmpty() ? mainStatus : status;
                        if ("Under Inspection".equalsIgnoreCase(effectiveStatus)) {
                            underInspection++;
                        } else if ("Pending".equalsIgnoreCase(effectiveStatus) || "Open".equalsIgnoreCase(effectiveStatus)) {
                            pending++;
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println("Error in getSleeperInspectionCallStatusDetailsFiltered: " + ex.getMessage());
                ex.printStackTrace();
            }
        } else {
            // Global overview calculated from all issued ICs
            long totalOfferedNos = 0L;
            long totalOfferedSet = 0L;

            List<Object[]> finalSummaryRows = sleeperFinalResultRepository.getAllSleeperFinalSummary();
            if (finalSummaryRows != null && !finalSummaryRows.isEmpty() && finalSummaryRows.get(0) != null) {
                Object[] row = finalSummaryRows.get(0);
                finalAcceptedNos = row[0] != null ? ((Number) row[0]).longValue() : 0L;
                finalAcceptedSet = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                finalRejectedNos = row[2] != null ? ((Number) row[2]).longValue() : 0L;
                finalRejectedSet = row[3] != null ? ((Number) row[3]).longValue() : 0L;
                if (row.length > 4 && row[4] != null) totalOfferedNos = ((Number) row[4]).longValue();
                if (row.length > 5 && row[5] != null) totalOfferedSet = ((Number) row[5]).longValue();
            }
            rejectedInFinal = finalRejectedNos + finalRejectedSet;
            try {
                Long demouldDefects = demouldingDefectiveSleeperRepository.countAllDemouldingDefectsForIssuedIcs();
                rejectedInProcess = demouldDefects != null ? demouldDefects : 0L;
            } catch (Exception ex) {
                System.err.println("Error in countAllDemouldingDefectsForIssuedIcs: " + ex.getMessage());
                rejectedInProcess = 0L;
            }

            if (totalOfferedNos > 0 || totalOfferedSet > 0) {
                totalProduction = totalOfferedNos + totalOfferedSet + rejectedInProcess;
            } else if (finalAcceptedNos > 0 || finalAcceptedSet > 0 || rejectedInFinal > 0) {
                totalProduction = finalAcceptedNos + finalAcceptedSet + rejectedInFinal + rejectedInProcess;
            } else {
                totalProduction = 0L;
            }

            long totalRejected = rejectedInProcess + rejectedInFinal;
            rejectionPercentage = totalProduction > 0 ? (totalRejected * 100.0) / totalProduction : 0.0;

            Long icCount = sleeperWorkflowRepository.countAllSleeperIcIssued();
            sleeperIcIssued = icCount != null ? icCount : 0L;

            java.util.Map<String, Long> callStatusCounts = getFinalInspectionCallStatusCounts();
            pending = callStatusCounts.getOrDefault("pending", 0L);
            underInspection = callStatusCounts.getOrDefault("underInspection", 0L);
        }

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("rejectedInProcess", rejectedInProcess);
        result.put("rejectedInFinal", rejectedInFinal);
        result.put("rejectionPercentage", Math.round(rejectionPercentage * 100.0) / 100.0);
        result.put("pendingCalls", pending);
        result.put("underInspectionCalls", underInspection);
        result.put("totalProduction", totalProduction);
        result.put("sleeperAvgProductionPerDay", sleeperAvgProductionPerDay);
        result.put("sleeperIcIssued", sleeperIcIssued);
        result.put("finalAcceptedNos", finalAcceptedNos);
        result.put("finalAcceptedSet", finalAcceptedSet);
        result.put("finalRejectedNos", finalRejectedNos);
        result.put("finalRejectedSet", finalRejectedSet);
        return result;
    }


}

