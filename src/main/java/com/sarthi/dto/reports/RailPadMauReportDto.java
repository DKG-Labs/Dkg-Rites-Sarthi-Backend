package com.sarthi.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RailPadMauReportDto {
    private String plantName;
    private String rio;
    private Long production;
    private Long acceptance;
    private Long processRejection;
    private Double processRejPct;
    private Long finalRejection;
    private Double finalRejPct;
    private Double totalRejPct;
}
