package com.sarthi.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RailPadPoLifeCycle1stLevelDto {
    private Integer slNo;
    private String rly;
    private String poNo;
    private LocalDateTime poDate;
    private String vendor;
    private String rio;
    private Long totalQty;
    private Long acceptedQty;
    private Long overallPoBalance;
    private String railPadType;
}
