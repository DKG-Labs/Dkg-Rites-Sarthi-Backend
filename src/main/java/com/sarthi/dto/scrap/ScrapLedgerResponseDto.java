package com.sarthi.dto.scrap;

import lombok.Data;

import java.util.List;

@Data
public class ScrapLedgerResponseDto {
    private String poiCode;
    private String unitLineShedNo;
    private Integer totalRejected;
    private Integer totalScrapped;
    private Integer pendingBalance;
    private List<ScrapLedgerRowDto> rows;
}
