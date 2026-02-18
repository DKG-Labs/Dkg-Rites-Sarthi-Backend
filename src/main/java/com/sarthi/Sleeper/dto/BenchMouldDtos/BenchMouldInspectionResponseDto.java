package com.sarthi.Sleeper.dto.BenchMouldDtos;

import lombok.Data;

@Data
public class BenchMouldInspectionResponseDto {
       private Long id;

        private String lineShedNo;

        private String checkingDate;

        private String benchGangNo;

        private String sleeperType;

        private String latestCastingDate;

        private String benchVisualResult;
        private String benchDimensionalResult;

        private String mouldVisualResult;
        private String mouldDimensionalResult;

        private String combinedRemarks;
}

