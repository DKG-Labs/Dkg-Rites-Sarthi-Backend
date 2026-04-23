package com.sarthi.Sleeper.dto.MainIeInspectionDtos;

import lombok.Data;

import java.util.List;

@Data
public class SleeperInspectionBatchDetailDTO {

        private String batchNo;

        private String castingDate;
        private Integer totalSleepersCasted;

        private Integer offeredNow;
        private Integer passed;
        private Integer rejected;
        private Integer unoffered;

        private List<String> acceptedSleepers;
        private List<String> rejectedSleepers;

        private List<String> etSleepers; // null

}
