package com.sarthi.Sms.dto.welding;

import lombok.Data;

@Data
public class WeldingInspectionDetailDto {
    private String jointNumber;
    private Integer inspectionMasterId;
    private Boolean weldParameter;
    private Boolean visual;
    private Boolean marking;
    private Boolean dimension;
    private String dimensionDetail;
    private Boolean usfd;
    private String usfdDetail;
    private String weldOperator;
    private String usfdOperator;
    private String result;
    private String railId1;
    private String railId1Length;
    private String railId2;
    private String railId2Length;
    private String remark;
}

