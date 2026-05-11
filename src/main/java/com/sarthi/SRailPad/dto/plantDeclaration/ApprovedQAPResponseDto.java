package com.sarthi.SRailPad.dto.plantDeclaration;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ApprovedQAPResponseDto {
    private Long id;
    private String vendorName;
    private String vendorCode;
    private String plantId;
    private String shift;
    private String qapNo;
    private String approvingAuthority;
    private LocalDate approvalDate;
    private LocalDate effectiveDate;
    private LocalDate validityDate;
    
    private List<ProductDetailResponseDto> productDetails;

    private String status;
    private Long createdBy;
    private LocalDateTime createdDate;
    private Long updatedBy;
    private LocalDateTime updatedDate;

    @Data
    public static class ProductDetailResponseDto {
        private Long id;
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
