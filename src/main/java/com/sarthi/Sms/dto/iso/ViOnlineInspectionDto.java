package com.sarthi.Sms.dto.iso;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ViOnlineInspectionDto {
    private String heatNumber;
    private String heatStatus;
    private String railId;
    private Boolean gaugingEndStraightness;
    private String utFeedback;
    private String defectPresent;
    private BigDecimal actualOfferedLength;
    private String acceptanceClassA;
    private String acceptanceClassAPlus;
    private String rejectedLengthDetails;
    private String defectAnalysis;
    private String remarks;

    public ViOnlineInspectionDto(Object[] obj) {
        this.heatNumber = (String) obj[0];
        this.heatStatus = (String) obj[1];
        this.railId = (String) obj[2];
        this.gaugingEndStraightness = obj[3] != null ? (Boolean) obj[3] : null;
        this.utFeedback = (String) obj[4];
        this.defectPresent = (String) obj[5];
        this.actualOfferedLength = obj[6] != null ? new BigDecimal(obj[6].toString()) : BigDecimal.ZERO;
        this.acceptanceClassA = obj[7] != null ? (String) obj[7] : "";
        this.acceptanceClassAPlus = obj[8] != null ? (String) obj[8] : "";
        this.rejectedLengthDetails = obj[9] != null ? (String) obj[9] : "";
        this.defectAnalysis = obj[10] != null ? (String) obj[10] : "";
        this.remarks = obj[11] != null ? (String) obj[11] : "";
    }
}
