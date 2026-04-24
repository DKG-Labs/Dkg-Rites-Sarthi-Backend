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
public class FinalDimensionalAnnexureResponseDTO {
    private String inspectionCallNo;
    private String manufacturer;
    private String vendor;
    private String certificateNo;
    private String productName;
    private String dateOfInspection;
    
    private List<FinalDimensionalAnnexurePageDTO> pages;
}
