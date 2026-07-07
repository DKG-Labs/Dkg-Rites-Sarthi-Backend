package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SleeperPoWiseAnalysisDTO {


        private String rlyZone;
        private String poNumber;
        private LocalDate poDate;
        private Long poQty;

        private String plantName;
        private String inspectedBy;   // RIO

        private Long production;
        private Long acceptance;

        private Long processRejection;
        private Long finalRejection;

        private Double rejectionPercentage;

        private Long noOfPos;
        private String uom;

}
