package com.sarthi.Sleeper.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MorTestRequestDto {

        private Long morSampleId;

        private String testingDate;

        private Double weight;

        private Double loadKn;

        private Double strength;

        private String remarks;

        private Long createdBy;

        private String shift;
        private String vendorCode;
        private String plantId;

        private LocalDate samplingDate;

        private String concreteGrade;

        private String sampleIdentificationNumber;

}
