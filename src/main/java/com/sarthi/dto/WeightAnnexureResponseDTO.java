package com.sarthi.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeightAnnexureResponseDTO {
    private String inspectionCallNo;
    private String manufacturer;
    private String vendor;
    private String certificateNo;
    private String productName;
    private String dateOfInspection;
    
    // Each page corresponds to a sampling round
    private List<WeightAnnexurePageDTO> pages;
}
