package com.sarthi.Sleeper.dto;

import lombok.Data;

import java.util.List;

/**
 * Slim list view of a moisture analysis entry. Same JSON shape as
 * MoistureAnalysisResponseDTO but only the fields the list screens read.
 * Full detail is still available from /MoistureAnalysis/{id}.
 */
@Data
public class MoistureAnalysisLiteDTO {

    private Long id;
    private String entryDate;
    private String shift;
    private String entryTime;
    private String batchNo;
    private String plantId;

    private Double totalFreeMoisture;
    private Double adjustedWaterWt;

    private String status;

    private List<Section> sections;

    @Data
    public static class Section {
        private String sectionType; // CA1 / CA2 / FA
        private Double freeMoisturePercent;
    }
}
