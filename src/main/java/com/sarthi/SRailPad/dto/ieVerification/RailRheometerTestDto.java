package com.sarthi.SRailPad.dto.ieVerification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RailRheometerTestDto {

    private Long id;

    @NotBlank(message = "Plant ID is mandatory")
    private String plantId;

    @NotBlank(message = "Vendor Code is mandatory")
    private String vendorCode;

    @NotBlank(message = "Batch Number is mandatory")
    private String batchNo;

    @NotNull(message = "Vulcanization Time is mandatory")
    private Double vulcanTime;

    @NotNull(message = "Vulcanization Temp is mandatory")
    private Double vulcanTemp;

    @NotBlank(message = "Validation check is mandatory")
    private String ensured;

    private String status;

    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
