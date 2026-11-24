package com.sarthi.Sms.dto.welding;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class WeldingDetailDto {
    private String jointNo;
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
    private BigDecimal railId1Length;
    private String railId2;
    private BigDecimal railId2Length;
    private String remark;
}

