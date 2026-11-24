package com.sarthi.Sms.dto.dashboard;

public class AiSystemRailDto {
    private String railId;
    private String heatNumber;
    private String surfaceDefectDetection;
    private String dimensionalVariationDetection;
    private String ocr;

    public AiSystemRailDto() {}

    public AiSystemRailDto(String railId, String heatNumber, String surfaceDefectDetection,
                          String dimensionalVariationDetection, String ocr) {
        this.railId = railId;
        this.heatNumber = heatNumber;
        this.surfaceDefectDetection = surfaceDefectDetection;
        this.dimensionalVariationDetection = dimensionalVariationDetection;
        this.ocr = ocr;
    }

    public String getRailId() { return railId; }
    public void setRailId(String railId) { this.railId = railId; }

    public String getHeatNumber() { return heatNumber; }
    public void setHeatNumber(String heatNumber) { this.heatNumber = heatNumber; }

    public String getSurfaceDefectDetection() { return surfaceDefectDetection; }
    public void setSurfaceDefectDetection(String surfaceDefectDetection) {
        this.surfaceDefectDetection = surfaceDefectDetection;
    }

    public String getDimensionalVariationDetection() { return dimensionalVariationDetection; }
    public void setDimensionalVariationDetection(String dimensionalVariationDetection) {
        this.dimensionalVariationDetection = dimensionalVariationDetection;
    }

    public String getOcr() { return ocr; }
    public void setOcr(String ocr) { this.ocr = ocr; }
}
