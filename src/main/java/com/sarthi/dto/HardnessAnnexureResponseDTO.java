package com.sarthi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Root DTO for the Final Hardness Test Annexure.
 * Contains metadata and multiple pages (one per Lot/Heat + Sampling).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HardnessAnnexureResponseDTO {
    private String inspectionCallNo;
    private String manufacturer;
    private String vendor;
    private String certificateNo;
    private String productName;
    private String dateOfInspection;
    
    // Multiple pages for multiple lots, heats, or samplings
    private List<HardnessAnnexurePageDTO> pages;
}
