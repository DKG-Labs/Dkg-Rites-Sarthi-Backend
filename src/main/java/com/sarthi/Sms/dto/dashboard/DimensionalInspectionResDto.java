package com.sarthi.Sms.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DimensionalInspectionResDto {
    private int inspectionId;
    private String railId;
    private int cameraId;
    private String baseImagePath;
    private BigDecimal edgeDiff;
    private BigDecimal chopDiff;
    private BigDecimal imageDiff;
    private String actualStatus;
    private String resultStatus;
    private String confusionClassifier;
    private LocalDateTime inspectedAt;
    private String operatorId;
    private String dutyId;
    private String shift;
    private String defectType;
    private BigDecimal distanceFromHead;
    private BigDecimal dimensionDeviation;
    private Integer noOfDimensionVariation;
    private String cameraName;

    // Manual getters and setters to avoid Lombok issues
    public int getInspectionId() {
        return inspectionId;
    }

    public void setInspectionId(int inspectionId) {
        this.inspectionId = inspectionId;
    }

    public String getRailId() {
        return railId;
    }

    public void setRailId(String railId) {
        this.railId = railId;
    }

    public int getCameraId() {
        return cameraId;
    }

    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
    }

    public String getBaseImagePath() {
        return baseImagePath;
    }

    public void setBaseImagePath(String baseImagePath) {
        this.baseImagePath = baseImagePath;
    }

    public BigDecimal getEdgeDiff() {
        return edgeDiff;
    }

    public void setEdgeDiff(BigDecimal edgeDiff) {
        this.edgeDiff = edgeDiff;
    }

    public BigDecimal getChopDiff() {
        return chopDiff;
    }

    public void setChopDiff(BigDecimal chopDiff) {
        this.chopDiff = chopDiff;
    }

    public BigDecimal getImageDiff() {
        return imageDiff;
    }

    public void setImageDiff(BigDecimal imageDiff) {
        this.imageDiff = imageDiff;
    }

    public String getActualStatus() {
        return actualStatus;
    }

    public void setActualStatus(String actualStatus) {
        this.actualStatus = actualStatus;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getConfusionClassifier() {
        return confusionClassifier;
    }

    public void setConfusionClassifier(String confusionClassifier) {
        this.confusionClassifier = confusionClassifier;
    }

    public LocalDateTime getInspectedAt() {
        return inspectedAt;
    }

    public void setInspectedAt(LocalDateTime inspectedAt) {
        this.inspectedAt = inspectedAt;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getDutyId() {
        return dutyId;
    }

    public void setDutyId(String dutyId) {
        this.dutyId = dutyId;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getDefectType() {
        return defectType;
    }

    public void setDefectType(String defectType) {
        this.defectType = defectType;
    }

    public BigDecimal getDistanceFromHead() {
        return distanceFromHead;
    }

    public void setDistanceFromHead(BigDecimal distanceFromHead) {
        this.distanceFromHead = distanceFromHead;
    }

    public BigDecimal getDimensionDeviation() {
        return dimensionDeviation;
    }

    public void setDimensionDeviation(BigDecimal dimensionDeviation) {
        this.dimensionDeviation = dimensionDeviation;
    }

    public Integer getNoOfDimensionVariation() {
        return noOfDimensionVariation;
    }

    public void setNoOfDimensionVariation(Integer noOfDimensionVariation) {
        this.noOfDimensionVariation = noOfDimensionVariation;
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }
}
