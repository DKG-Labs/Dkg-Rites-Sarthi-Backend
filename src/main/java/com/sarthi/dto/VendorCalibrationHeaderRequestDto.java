package com.sarthi.dto;

import lombok.Data;
import java.util.List;

@Data
public class VendorCalibrationHeaderRequestDto {
    private Long id;
    private String vendorCode;
    private String category;
    private String certificateFilePath;
    private String certificateFileBase64; // Option for base64 file upload
    private List<VendorCalibrationDetailDto> details;
}
