package com.sarthi.Sleeper.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MoistureAnalysisResponseDTO {

        private Long id;

        private String entryDate;
        private String shift;
        private String entryTime;
        private String batchNo;

        private String approvedMixDesign;

        private Double designAC;
        private Double designWC;
        private Double designCement;
        private Double designCA1;
        private Double designCA2;
        private Double designFA;
        private Double designWater;
        private Double designAdmix;

        private Double actualCement;
        private Double actualCA1;
        private Double actualCA2;
        private Double actualFA;
        private Double actualWater;
        private Double actualAdmix;

        private Double wtAdoptedCa1;
        private Double wtAdoptedCa2;
        private Double wtAdoptedFa;
        private Double totalFreeMoisture;
        private Double adjustedWaterWt;
        private Double wcRatio;
        private Double acRatio;

        private List<MoistureSectionDTO> sections;

        private int createdBy;
        private int updatedBy;

        private String status;

}
