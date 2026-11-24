package com.sarthi.Sms.dto.welding;


import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class WeldCombinedDto {
    private Integer inspectionMasterId;
    private String panelId;
    private String dutyId;
    private Integer noOfJoints;
    private Integer panelLength;
    private String panelDecision;
    private String panelRemark;
    private String frontResult;
    private String backResult;
    private String frontRemarkForNotOk;
    private String backRemarkForNotOk;
    private String frontRemark;
    private String backRemark;

    private List<WeldingDetailDto> details;

    @Data
    public static class WeldingDetailDto {
        private String jointNo;
        private Boolean weldParameter;
        private Boolean marking;
        private Boolean visual;
        private Boolean dimension;
        private String dimensionDetail;
        private Boolean usfd;
        private String usfdDetail;
        private String weldOperator;
        private String usfdOperator;
        private String result;
        private String railId1;
        private String railId2;
        private BigDecimal railId1Length;
        private BigDecimal railId2Length;
        private String remark;
    }
}
