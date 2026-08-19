package com.sarthi.dto.reports;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BasicDetailsDto {

    private LocalDate date;
    private String shift;
    private String lineNo;
    private String engineer;
    private String rlyName;
    private String poSrNo;
    private String lotNumber;
    private LocalDateTime createdAt;

    private Integer totalAcceptedQty;
    private Integer totalRejectionQty;
}
