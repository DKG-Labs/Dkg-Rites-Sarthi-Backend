package com.sarthi.dto.Calibration;


import lombok.Data;
import java.util.List;

@Data
public class CreateIeVendorCalibrationInspectionRequestDto {

    private String callNo;

    private String poNumber;

    private String vendorCode;

    private List<IeVendorCalibrationInspectionDetailRequestDto> details;
}
