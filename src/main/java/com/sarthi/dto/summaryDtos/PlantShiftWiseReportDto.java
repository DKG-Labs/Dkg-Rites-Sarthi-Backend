package com.sarthi.dto.summaryDtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PlantShiftWiseReportDto {

        private LocalDate inspectionDate;

        private String shift;

        private List<String> lotNumbers;

        private List<String> poNumbers;

        private Integer productionInShearing;

        private Integer productionInTempering;

        private Integer acceptedQtyInTempering;

        private Integer totalRejected;

}
