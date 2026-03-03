package com.sarthi.Sleeper.dto.Cement;

import lombok.Data;

@Data
public class CementBatchDetailsResponseDto {

    private Long id;

    private Integer weekNo;
    private Integer yearNo;
    private String mtcNo;
    private Double quantityKg;
}