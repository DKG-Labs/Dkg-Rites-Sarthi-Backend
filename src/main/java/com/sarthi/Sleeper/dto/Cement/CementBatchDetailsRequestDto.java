package com.sarthi.Sleeper.dto.Cement;

import lombok.Data;

@Data
public class CementBatchDetailsRequestDto {

    private Integer weekNo;
    private Integer yearNo;
    private String mtcNo;
    private Double quantityKg;
}