package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SleeperEmpPerformanceDto {

        private String companyName;

        private String plantName;

        private String plantId;

        private String rio;

        private String ieName;

        private String stageOfInspection;

        private String shift;

        private Long rejectedSleepers;

        private Long shiftsWorked;

}
