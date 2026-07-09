package com.sarthi.dto.summaryDtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PoWiseInspectionTrackingDTO {

        private Integer sno;

        private String zonalRailway;
        private String vendor;
        private String ercType;

        private String poNumber;
        private LocalDate poDate;

        private String specification;

        private Double poQty;

        // Process
        private Double processInspectedQty;          // SUM(shearing_manufactured)
        private Double processAcceptedQty;           // SUM(tempering_accepted)

        // Final
        private Double offeredForFinalInspectionQty; // SUM(qty_now_offered)
        private Double finalAcceptedQty;             // SUM(qty_now_passed)

        // IC Details
        private Long noOfIcIssued;                   // RM + PROCESS + FINAL IC count
        private LocalDate lastIcIssuedDate;          // MAX(final IC generated date)

        private Long totalRejectedNos;

        /* Raw Material Defects */
        private Long chemicalCompositionRej;
        private Long diameterBarRej;
        private Long grainSizeRej;
        private Long inclusionRatingRej;
        private Long depthOfDecarbRej;
        private Long hardnessRawRej;

        /* Process Defects */
        private Long shearingRej;
        private Long mpiRej;
        private Long turningRej;
        private Long forgingRej;
        private Long quenchingRej;
        private Long temperingRej;
        private Long dimensionFinishedErcRej;
        private Long hardnessProcessRej;

        private Long depthOfDecarburizationRej;
        private Long dimensionToleranceRej;
        private Long applicationAndDeflectionTestRej;
        private Long toeLoadTestRej;
        private Long weightRej;
        private Long visualTestRej;
        private Long microStructureRej;
        private Long freedomFromDefectsRej;
        private Long otherRejections;
        private String remarks;

        private Long totalRejections;

        private Double rejectionPercentage;
}