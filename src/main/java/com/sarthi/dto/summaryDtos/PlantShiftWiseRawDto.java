package com.sarthi.dto.summaryDtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class PlantShiftWiseRawDto {

        private LocalDate inspectionDate;

        private String shift;

        private String lotNumber;

        private String poNo;

        private String poSerialNo;

        private Integer shearingManufactured;

        private Integer temperingManufactured;

        private Integer temperingAccepted;

        private Integer totalRejected;

}
