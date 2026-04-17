package com.sarthi.Sleeper.dto;


import lombok.Data;

import java.time.LocalDate;

@Data
public class MfTestDetailsResponseDto {

    private Long id;

    private Long modulusOfFailureId;

    private LocalDate testingDate;

    private Double strength;

    private Double finalStrength;

    private String result;

    private String remarks;

    private String shift;
    private String vendorCode;
    private String plantId;

    private Long createdBy;


}