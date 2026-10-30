package com.sarthi.dto.scrap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardSummaryRowDto {
    private String poiCode;
    private String companyName;
    private String unitName;
    private long totalRejected;
    private long totalScrapped;
    private long pendingBalance;
    private LocalDate lastScrappingDate;
}
