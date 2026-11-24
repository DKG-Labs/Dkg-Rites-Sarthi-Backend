package com.sarthi.Sms.dto.dashboard;

public class AiAccuracyMetricsDto {
    private String railId;
    private SurfaceDefectMetrics surfaceDefectDetection;
    private DimensionalVariationMetrics dimensionalVariationDetection;
    private String ocr;

    public AiAccuracyMetricsDto() {}

    public AiAccuracyMetricsDto(String railId, SurfaceDefectMetrics surfaceDefectDetection,
                               DimensionalVariationMetrics dimensionalVariationDetection, String ocr) {
        this.railId = railId;
        this.surfaceDefectDetection = surfaceDefectDetection;
        this.dimensionalVariationDetection = dimensionalVariationDetection;
        this.ocr = ocr;
    }

    public String getRailId() { return railId; }
    public void setRailId(String railId) { this.railId = railId; }

    public SurfaceDefectMetrics getSurfaceDefectDetection() { return surfaceDefectDetection; }
    public void setSurfaceDefectDetection(SurfaceDefectMetrics surfaceDefectDetection) {
        this.surfaceDefectDetection = surfaceDefectDetection;
    }

    public DimensionalVariationMetrics getDimensionalVariationDetection() { return dimensionalVariationDetection; }
    public void setDimensionalVariationDetection(DimensionalVariationMetrics dimensionalVariationDetection) {
        this.dimensionalVariationDetection = dimensionalVariationDetection;
    }

    public String getOcr() { return ocr; }
    public void setOcr(String ocr) { this.ocr = ocr; }

    public static class SurfaceDefectMetrics {
        private String precision;
        private String recall;

        public SurfaceDefectMetrics() {}
        public SurfaceDefectMetrics(String precision, String recall) {
            this.precision = precision;
            this.recall = recall;
        }

        public String getPrecision() { return precision; }
        public void setPrecision(String precision) { this.precision = precision; }

        public String getRecall() { return recall; }
        public void setRecall(String recall) { this.recall = recall; }
    }

    public static class DimensionalVariationMetrics {
        private String precision;
        private String recall;

        public DimensionalVariationMetrics() {}
        public DimensionalVariationMetrics(String precision, String recall) {
            this.precision = precision;
            this.recall = recall;
        }

        public String getPrecision() { return precision; }
        public void setPrecision(String precision) { this.precision = precision; }

        public String getRecall() { return recall; }
        public void setRecall(String recall) { this.recall = recall; }
    }
}
