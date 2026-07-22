package com.sarthi.SRailPad.dto.ieVerification;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RailMouldVerificationDto {

    private Long id;

    @NotBlank(message = "Plant ID is mandatory")
    private String plantId;

    @NotBlank(message = "Vendor Code is mandatory")
    private String vendorCode;

    private String shift;
    private String castingDate;

    @NotBlank(message = "Mould Number is mandatory")
    private String mouldNumber;

    private String timeOfCheck;
    private String dimensionalAccuracy;
    private String dimensionalRemarks;
    private String freedomFromDefects;
    private String defectsRemarks;
    private String visualRemarks;
    private String status;

    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
