package com.sarthi.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RailPadPoLifeCycle2ndLevelDto {
    private Integer slNo;
    private String srNo;
    private String railPadType;
    private String consignee;
    private LocalDateTime originalDpDate;
    private LocalDateTime extendedDpDate;
    private Integer poSrNoQty;
    private Integer balanceQty;
    private Integer noOfIcs;
    private Double processRejectionPercentage;
    private Double finalRejectionPercentage;
    private Double totalRejectionPercentage;
}
