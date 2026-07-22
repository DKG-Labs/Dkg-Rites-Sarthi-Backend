package com.sarthi.SRailPad.dto.ieVerification;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RailVisualInspectionDto {

    private Long id;

    @NotBlank(message = "Plant ID is required")
    private String plantId;

    @NotBlank(message = "Vendor Code is required")
    private String vendorCode;

    private String shift;

    private String castingDate;

    @NotBlank(message = "Time of Check is required")
    private String timeOfCheck;

    private Integer sampleQuantity;

    @NotBlank(message = "Clear Cut Sides check is required")
    private String clearCutSides;

    @NotBlank(message = "Smooth Surface check is required")
    private String smoothSurface;

    private String defectRemarks;

    private String status;

    private String timestamp;
}
