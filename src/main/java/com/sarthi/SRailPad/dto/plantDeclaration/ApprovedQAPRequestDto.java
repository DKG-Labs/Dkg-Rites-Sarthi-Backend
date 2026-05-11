package com.sarthi.SRailPad.dto.plantDeclaration;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ApprovedQAPRequestDto {
    private String vendorName;
    private String vendorCode;
    private String plantId;
    private String shift;
    private String qapNo;
    private String approvingAuthority;
    private LocalDate approvalDate;
    private LocalDate effectiveDate;
    private LocalDate validityDate;
    
    private List<ProductDetailDto> productDetails;

    private Long createdBy;
    private Long updatedBy;

    @Data
    public static class ProductDetailDto {
        private String padType;
        // Mixing Parameters
        private Double minMixingTime;
        private Double maxMixingTime;
        private Double minMixingTemp;
        private Double maxMixingTemp;
        private Double mixingWeight;

        // Moulding Parameters
        private Double minCuringTime;
        private Double maxCuringTime;
        private Double minCuringTemp;
        private Double maxCuringTemp;
        private Double minCuringPressure;
        private Double maxCuringPressure;
    }
}
