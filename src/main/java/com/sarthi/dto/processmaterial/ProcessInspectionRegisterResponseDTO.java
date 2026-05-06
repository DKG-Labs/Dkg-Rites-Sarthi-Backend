package com.sarthi.dto.processmaterial;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ProcessInspectionRegisterResponseDTO {
    // Header Information
    private String date;
    private String shift;
    private String lotNo;
    private String poNoAndDate;
    private String caseNoIbs;
    private Integer ercProducedDuringShift;
    private String inspectingEngineerName;
    private String rawMaterialIcNoAndDate;
    private String ercType;
    private String heatNo;
    private String mfgName;
    private String callNoAndDate;
    private String lineNo;
    private String remarks;

    // Hourly Labels (e.g., ["06:00-07:00", "07:00-08:00", ...])
    private List<String> hourLabels;

    // Activity Rows
    private List<ProcessInspectionRegisterRowDTO> rows;
}
