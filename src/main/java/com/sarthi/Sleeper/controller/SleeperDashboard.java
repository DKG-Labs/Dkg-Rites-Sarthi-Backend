package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.SleeperDashboardDtos.*;
import com.sarthi.Sleeper.service.DashboardService;
import com.sarthi.dto.reports.IeOperationalSlaPerformanceSummaryDto;
import com.sarthi.dto.reports.IeWiseCallStatusWorkloadSummaryDto;
import com.sarthi.dto.reports.InspectionCallsReportDto;
import com.sarthi.util.CommonUtils;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/sleeper-dashboard")
public class SleeperDashboard {

    @Autowired
    private DashboardService  dashboardService;

    @GetMapping("/demoulding-process-rejected-count")
    public ResponseEntity<Object> getRejectedCount() {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(dashboardService.getRejectedSleepersCount()),
                HttpStatus.OK
        );
    }

    @GetMapping("/final-inspection-call-status-counts")
    public ResponseEntity<Object> getFinalInspectionCallStatusCounts() {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(dashboardService.getFinalInspectionCallStatusCounts()),
                HttpStatus.OK
        );
    }


    @GetMapping("/Final-inspection-rejected-count")
    public ResponseEntity<Object> getFinalInspectionRejectedCount() {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(dashboardService.getTotalRejectedCount()),
                HttpStatus.OK
        );
    }

    @GetMapping("/rejection-percentage")
    public ResponseEntity<Object> getRejectionPercentage() {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(dashboardService.getRejectionPercentage()),
                HttpStatus.OK
        );
    }

    @GetMapping("/monthly-analysis")
    public ResponseEntity<Object> getMonthlyAnalysis(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getMonthlyAnalysis(startDate, endDate)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/sleeper-powise-monthly-analysis")
    public ResponseEntity<Object> getPoWiseMonthlyAnalysis(
            @RequestParam(required = false) String plantId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getPoWiseAnalysis(
                               plantId,
                               startDate,
                                endDate)
                ),
                HttpStatus.OK
        );
    }



    @GetMapping("/LifeCycle/LotWise")
    public ResponseEntity<Object> getLifeCycleLotWise(
            @RequestParam Long id,
            @RequestParam String batchId) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getLifecycleReport(id, batchId)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/companies")
    public ResponseEntity<Object> getCompanies() {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getCompanies()
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/plants")
    public ResponseEntity<Object> getPlants(@RequestParam String vendorCode) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getPlants(vendorCode)
                ),
                HttpStatus.OK
        );
    }
    @GetMapping("/batches")
    public ResponseEntity<Object> getBatches(@RequestParam String plantId) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getBatches(plantId)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/level5BatchInspectionData")
    public ResponseEntity<Object> getLevel5api(   @RequestParam Long id,
                                                  @RequestParam String batchId) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getBatchChecking(batchId, id)
                ),
                HttpStatus.OK
        );
    }


    @GetMapping("/level4")
    public  ResponseEntity<Object> getLevel4(@RequestParam String callNo) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getLevel4Report(callNo)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/level3")
    public  ResponseEntity<Object> getLevel3(@RequestParam String poNo,
                                                @RequestParam String srNo) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getLevel3Report(poNo,srNo)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/level2")
    public  ResponseEntity<Object> getLevel2(@RequestParam String poNo) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getLevel2(poNo)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/level1")
    public ResponseEntity<Object> getLevel1(
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getLevel1(
                                start,
                                end
                        )
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/mpr")
    public ResponseEntity<Object> getMpr(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        // Default range: the current month
        LocalDate start = CommonUtils.parseDateFlexible(startDate, LocalDate.now().withDayOfMonth(1));
        LocalDate end = CommonUtils.parseDateFlexible(endDate, LocalDate.now());

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getMpr(start, end)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/manufacturer-performance/{plantId}")
    public ResponseEntity<Object>  getPerformance(
            @PathVariable(required = false) String plantId) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getLastYearPerformance(plantId)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/process-defect-distribution")
    public ResponseEntity<Object>  getProcessDefectDistribution(
            @RequestParam(required = false) String plantId) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getProcessDefectDistribution(plantId)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/defect-distribution-analysis")
    public ResponseEntity<Object> getDefectDistribution(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDate start = CommonUtils.parseDateFlexible(startDate, LocalDate.now().minusMonths(1));
        LocalDate end = CommonUtils.parseDateFlexible(endDate, LocalDate.now());

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getDefectReasonDistribution(start,end)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/pareto-analysis")
    public ResponseEntity<Object> getParetoAnalysis(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDate start = CommonUtils.parseDateFlexible(startDate, LocalDate.now().minusMonths(1));
        LocalDate end = CommonUtils.parseDateFlexible(endDate, LocalDate.now());

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getParetoAnalysis(start,end)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/employee-wise-performance")
    public ResponseEntity<Object> getEmployeeWisePerformance(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDate start = CommonUtils.parseDateFlexible(startDate, LocalDate.now().minusMonths(1));
        LocalDate end = CommonUtils.parseDateFlexible(endDate, LocalDate.now());

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getEmployeePerformance(start, end)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/shift-wise-production")
    public ResponseEntity<Object> getShiftWiseProductionReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String plantId) {

        LocalDate start = CommonUtils.parseDateFlexible(startDate, LocalDate.now().minusMonths(1));
        LocalDate end = CommonUtils.parseDateFlexible(endDate, LocalDate.now());

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                       dashboardService.getReport(start, end, plantId)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/qtyOfPSCSleepers")
    public ResponseEntity<Object> getQtyOfPscSleeperReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDate start = CommonUtils.parseDateFlexible(startDate, LocalDate.now().minusMonths(1));
        LocalDate end = CommonUtils.parseDateFlexible(endDate, LocalDate.now());

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getQtyPscSleeperReport(start, end)
                ),
                HttpStatus.OK
        );
    }


    /**
     * Get distinct company names from vendor_plant table
     * for Sleeper Shift Wise Production Report manufacturer dropdown
     */
    @GetMapping("/vendor-plant/companies")
    public ResponseEntity<Object> getVendorPlantCompanies(
            @RequestParam(required = false) String zone) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getVendorPlantCompanyNames(zone)
                ),
                HttpStatus.OK
        );
    }

    /**
     * Get plants by company name from vendor_plant table
     * for Sleeper Shift Wise Production Report plant dropdown
     */
    @GetMapping("/vendor-plant/plants")
    public ResponseEntity<Object> getVendorPlantsByCompany(
            @RequestParam String companyName) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getVendorPlantsByCompanyName(companyName)
                ),
                HttpStatus.OK
        );
    }


    @GetMapping("/cm-inspection-calls")
    public ResponseEntity<Object> getInspectionCallsReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        List<InspectionCallsReportDto>  list = dashboardService.getInspectionCallsReport(startDate, endDate);

        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/cm-sleeper-overduecalls")
    public ResponseEntity<Object> getCmSleeperOverDueCalls(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        List<InspectionCallsReportDto>  list = dashboardService.getSleeperOverduePendingCallsReport(startDate, endDate);

        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/cm-sleeper-ie-callWiseStatus")
    public ResponseEntity<Object> getCmSleeperieCallWiseStatus(
            @RequestParam(required = false) String cmEmplId) {

        List<IeWiseCallStatusWorkloadSummaryDto>  list = dashboardService.getSleeperIeWiseCallStatusWorkloadSummary(cmEmplId);

        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }


    @GetMapping("/cm-sleeper-IECompletedCalls")
    public ResponseEntity<Object> getCmSleeperIeCompletedCalls(
            @RequestParam(required = false) String cmEmplId) {

        List<IeOperationalSlaPerformanceSummaryDto>  list = dashboardService.getSleeperIeOperationalSlaPerformanceSummary(cmEmplId);

        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/quality-sleeper")
    public ResponseEntity<Object> getQualitySleeper(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String zone) {

        // Identical requests share one result for a few minutes, and requests that arrive while it is being computed
        // wait for that same computation instead of running the queries again
        String key = String.join("|", String.valueOf(startDate), String.valueOf(endDate), String.valueOf(zone));
        long now = System.currentTimeMillis();
        java.util.concurrent.atomic.AtomicBoolean owner = new java.util.concurrent.atomic.AtomicBoolean(false);
        QualityCacheEntry entry = qualityCache.compute(key, (k, old) -> {
            if (old != null && now - old.createdAt() < QUALITY_CACHE_TTL_MS && !old.result().isCompletedExceptionally()) {
                return old;
            }
            owner.set(true);
            return new QualityCacheEntry(now, new java.util.concurrent.CompletableFuture<>());
        });
        if (qualityCache.size() > 100) {
            qualityCache.clear();
        }

        if (owner.get()) {
            try {
                entry.result().complete(dashboardService.getQualitySleeperReport(startDate, endDate, zone));
            } catch (Exception e) {
                entry.result().completeExceptionally(e);
                qualityCache.remove(key, entry);
                throw e;
            }
        }
        List<QualitySleeperReportDto> list;
        try {
            list = entry.result().get();
        } catch (java.util.concurrent.ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    private static final long QUALITY_CACHE_TTL_MS = 3 * 60 * 1000L;

    private record QualityCacheEntry(long createdAt,
                                     java.util.concurrent.CompletableFuture<List<QualitySleeperReportDto>> result) {}

    private final java.util.concurrent.ConcurrentHashMap<String, QualityCacheEntry> qualityCache =
            new java.util.concurrent.ConcurrentHashMap<>();

    @GetMapping("sleeperIc/{callNo}")
    public SleeperIcProjection getSleeperIcData(
            @PathVariable String callNo) {

        return dashboardService.getSleeperIcData(callNo);
    }

    /**
     * Consolidated Sleeper Dashboard Summary
     * Supports optional vendor, zonal railway, and date range filtering.
     * GET /api/sleeper-dashboard/summary?vendor={vendor}&zone={zone}&startDate={startDate}&endDate={endDate}
     */
    // The summary runs several heavy queries but changes slowly, so identical requests share one result for a few
    // minutes; requests that arrive while it is being computed wait for that same computation instead of repeating it.
    private static final long SUMMARY_CACHE_TTL_MS = 10 * 60 * 1000L;
    private static final int SUMMARY_CACHE_MAX_ENTRIES = 200;

    private record SummaryCacheEntry(long createdAt, java.util.concurrent.CompletableFuture<Object> result) {}

    private final java.util.concurrent.ConcurrentHashMap<String, SummaryCacheEntry> summaryCache =
            new java.util.concurrent.ConcurrentHashMap<>();

    private static String summaryCacheKey(String vendor, String zone, String startDate, String endDate) {
        return String.join("|", String.valueOf(vendor), String.valueOf(zone),
                String.valueOf(startDate), String.valueOf(endDate));
    }

    /**
     * Keeps the dashboard's default view (current financial year, all vendors and zones) ready, so opening the
     * dashboard never waits on the heavy queries. The new result replaces the old one only once it is finished.
     */
    @org.springframework.scheduling.annotation.Scheduled(initialDelay = 5000, fixedDelay = 4 * 60 * 1000L)
    public void warmDefaultSummary() {
        try {
            java.time.LocalDate today = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"));
            int fyStartYear = today.getMonthValue() >= 4 ? today.getYear() : today.getYear() - 1;
            String start = java.time.LocalDate.of(fyStartYear, 4, 1).toString();
            String end = today.toString();
            Object value = ResponseBuilder.getSuccessResponse(
                    dashboardService.getSleeperDashboardSummary(null, null, start, end));
            summaryCache.put(summaryCacheKey(null, null, start, end),
                    new SummaryCacheEntry(System.currentTimeMillis(),
                            java.util.concurrent.CompletableFuture.completedFuture(value)));
        } catch (Exception e) {
            System.err.println("[SleeperSummary] warm-up failed: " + e.getMessage());
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<Object> getSleeperDashboardSummary(
            @RequestParam(required = false) String vendor,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String plantId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "false") boolean refresh) throws Exception {
        String effectiveVendor = (vendor != null && !vendor.isBlank()) ? vendor : plantId;
        String key = summaryCacheKey(effectiveVendor, zone, startDate, endDate);

        if (summaryCache.size() > SUMMARY_CACHE_MAX_ENTRIES) {
            summaryCache.clear();
        }

        long now = System.currentTimeMillis();
        java.util.concurrent.atomic.AtomicBoolean owner = new java.util.concurrent.atomic.AtomicBoolean(false);
        SummaryCacheEntry entry = summaryCache.compute(key, (k, old) -> {
            boolean reusable = old != null && !refresh
                    && now - old.createdAt() < SUMMARY_CACHE_TTL_MS
                    && !old.result().isCompletedExceptionally();
            if (reusable) {
                return old;
            }
            owner.set(true);
            return new SummaryCacheEntry(now, new java.util.concurrent.CompletableFuture<>());
        });

        if (owner.get()) {
            try {
                entry.result().complete(ResponseBuilder.getSuccessResponse(
                        dashboardService.getSleeperDashboardSummary(effectiveVendor, zone, startDate, endDate)));
            } catch (Exception e) {
                entry.result().completeExceptionally(e);
                summaryCache.remove(key, entry);
                throw e;
            }
        }
        return new ResponseEntity<>(entry.result().get(), HttpStatus.OK);
    }

    @Autowired(required = false)
    private com.sarthi.service.reports reportService;

    @GetMapping("/debug-reports")
    public ResponseEntity<Object> debugReports() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        try {
            if (reportService != null) {
                map.put("dashboardSummary", reportService.getDashboardSummary(null, null, null, null));
            }
        } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            map.put("dashboardSummaryError", sw.toString());
        }
        try {
            if (reportService != null) {
                map.put("inspectionCallStatus", reportService.getInspectionCallStatus(null, null, null, null, "Sleeper"));
            }
        } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            map.put("inspectionCallStatusError", sw.toString());
        }
        return ResponseEntity.ok(map);
    }
}
