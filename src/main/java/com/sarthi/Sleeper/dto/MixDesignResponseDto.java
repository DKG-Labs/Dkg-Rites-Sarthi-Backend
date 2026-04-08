package com.sarthi.Sleeper.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MixDesignResponseDto {

    private Long id;

    private String identification;
    private String concreteGrade;
    private String authorityOfApproval;

    private Double cement;
    private Double ca1;
    private Double ca2;
    private Double fa;
    private Double water;

    private Double acRatio;
    private Double wcRatio;

    private Integer createdBy;
    private LocalDateTime createdDate;

    private Integer updatedBy;
    private LocalDateTime updatedDate;

    private String status;

    private String vendorCode;
    private String plantId;

    private BigDecimal admixtureKg;
    private BigDecimal admixturePresentage;
}
