package com.sarthi.dto.Calibration;


import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class IeVendorCalibrationInspectionResponseDto {

    private Long id;

    private String callNo;

    private String poNumber;

    private String vendorCode;

    private String createdBy;

    private LocalDateTime createdDate;

    private String updatedBy;

    private LocalDateTime updatedDate;

    private List<IeVendorCalibrationInspectionDetailResponseDto> details;
}