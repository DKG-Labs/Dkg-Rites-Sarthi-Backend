package com.sarthi.Sleeper.dto;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class MixDesignRequestDto {

    private String identification;
    private String concreteGrade;
    private String authorityOfApproval;

    private Double cement;
    private Double ca1;
    private Double ca2;
    private Double fa;
    private Double water;

    private Integer createdBy;
    private Integer updatedBy;

    private Double acRatio;
    private Double wcRatio;
}