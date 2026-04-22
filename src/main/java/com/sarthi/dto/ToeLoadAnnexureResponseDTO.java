package com.sarthi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Root DTO for the Final Toe Load Test Annexure (Annexure-XI).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToeLoadAnnexureResponseDTO {
    private String inspectionCallNo;
    private String manufacturer;
    private String vendor;
    private String certificateNo;
    private String productName;
    private String dateOfInspection;
    
    // Multiple pages for multiple samplings
    private List<ToeLoadAnnexurePageDTO> pages;
}
