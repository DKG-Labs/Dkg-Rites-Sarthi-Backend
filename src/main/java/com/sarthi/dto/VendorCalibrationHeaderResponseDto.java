package com.sarthi.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VendorCalibrationHeaderResponseDto {
    private Long id;
    private String vendorCode;
    private String category;
    private String certificateFilePath;
    private List<VendorCalibrationDetailDto> details;
    private String createdBy;
    private LocalDateTime createdDate;
    private String updatedBy;
    private LocalDateTime updatedDate;
}
