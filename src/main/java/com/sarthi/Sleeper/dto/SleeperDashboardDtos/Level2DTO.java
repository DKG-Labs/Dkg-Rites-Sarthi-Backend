package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class Level2DTO {
        private Integer sno;

        private String poNo;
        private String srNo;

        private String sleeperType;   // null
        private String consignee;

        private LocalDate dpDate;
        private LocalDate extDpDate;

        private String qtyWithUom;

        private Integer balance;

        private String ics;
        private String lastIc;

        private Double procRejPercent;
        private Double finalRejPercent;
        private Double totalRejPercent;

}
