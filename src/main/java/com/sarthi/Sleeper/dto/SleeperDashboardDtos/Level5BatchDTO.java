package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Level5BatchDTO {

        private Integer sno;
        private String dateShift;

        private Double steamCubeStrength;
        private Integer rejectedDemoulding;
        private Integer rejectedVisual;
        private Integer rejectedCritical;
        private Integer rejectedNonCritical;
        private Double waterCubeStrength;
        private Double mrValue;

}
