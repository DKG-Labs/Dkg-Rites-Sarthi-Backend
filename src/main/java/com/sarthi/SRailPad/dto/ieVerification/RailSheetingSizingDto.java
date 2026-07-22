package com.sarthi.SRailPad.dto.ieVerification;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RailSheetingSizingDto {

    private Long id;

    @NotBlank(message = "Plant ID is mandatory")
    private String plantId;

    @NotBlank(message = "Vendor Code is mandatory")
    private String vendorCode;

    @NotBlank(message = "Batch Number is mandatory")
    private String batchNo;

    @NotBlank(message = "Sheeting status is mandatory")
    private String sheeting;

    private String remarks;

    private String status;

    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
