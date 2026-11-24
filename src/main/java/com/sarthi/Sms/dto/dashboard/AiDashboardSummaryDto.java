package com.sarthi.Sms.dto.dashboard;

import java.util.List;

public class AiDashboardSummaryDto {
    private Integer totalRailIds;
    private String avgPrecisionSurfaceDefect;
    private String avgRecallSurfaceDefect;
    private String avgPrecisionDimensionalVariation;
    private String avgRecallDimensionalVariation;
    private String ocrAccuracy;
    private List<AiAccuracyMetricsDto> railDetails;

    // Pagination support
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public AiDashboardSummaryDto() {}

    public AiDashboardSummaryDto(Integer totalRailIds, String avgPrecisionSurfaceDefect,
                                String avgRecallSurfaceDefect, String avgPrecisionDimensionalVariation,
                                String avgRecallDimensionalVariation, String ocrAccuracy,
                                List<AiAccuracyMetricsDto> railDetails) {
        this.totalRailIds = totalRailIds;
        this.avgPrecisionSurfaceDefect = avgPrecisionSurfaceDefect;
        this.avgRecallSurfaceDefect = avgRecallSurfaceDefect;
        this.avgPrecisionDimensionalVariation = avgPrecisionDimensionalVariation;
        this.avgRecallDimensionalVariation = avgRecallDimensionalVariation;
        this.ocrAccuracy = ocrAccuracy;
        this.railDetails = railDetails;
    }

    public Integer getTotalRailIds() { return totalRailIds; }
    public void setTotalRailIds(Integer totalRailIds) { this.totalRailIds = totalRailIds; }

    public String getAvgPrecisionSurfaceDefect() { return avgPrecisionSurfaceDefect; }
    public void setAvgPrecisionSurfaceDefect(String avgPrecisionSurfaceDefect) {
        this.avgPrecisionSurfaceDefect = avgPrecisionSurfaceDefect;
    }

    public String getAvgRecallSurfaceDefect() { return avgRecallSurfaceDefect; }
    public void setAvgRecallSurfaceDefect(String avgRecallSurfaceDefect) {
        this.avgRecallSurfaceDefect = avgRecallSurfaceDefect;
    }

    public String getAvgPrecisionDimensionalVariation() { return avgPrecisionDimensionalVariation; }
    public void setAvgPrecisionDimensionalVariation(String avgPrecisionDimensionalVariation) {
        this.avgPrecisionDimensionalVariation = avgPrecisionDimensionalVariation;
    }

    public String getAvgRecallDimensionalVariation() { return avgRecallDimensionalVariation; }
    public void setAvgRecallDimensionalVariation(String avgRecallDimensionalVariation) {
        this.avgRecallDimensionalVariation = avgRecallDimensionalVariation;
    }

    public String getOcrAccuracy() { return ocrAccuracy; }
    public void setOcrAccuracy(String ocrAccuracy) { this.ocrAccuracy = ocrAccuracy; }

    public List<AiAccuracyMetricsDto> getRailDetails() { return railDetails; }
    public void setRailDetails(List<AiAccuracyMetricsDto> railDetails) { this.railDetails = railDetails; }

    // Pagination getters and setters
    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public boolean isHasNext() { return hasNext; }
    public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }

    public boolean isHasPrevious() { return hasPrevious; }
    public void setHasPrevious(boolean hasPrevious) { this.hasPrevious = hasPrevious; }
}
