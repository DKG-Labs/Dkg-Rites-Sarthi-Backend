package com.sarthi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalChemicalAnalysisResponseDTO {
    // Header information
    private String inspectionCallNo;
    private String manufacturer;
    private String vendor;
    private String certificateNo;
    private String productName;
    private String dateOfInspection;
    
    // Technical Data Rows
    private List<FinalChemicalAnalysisRowDTO> rows;
}
