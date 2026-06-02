package com.sarthi.dto.Calibration;

import lombok.Data;

@Data
public class IeVendorCalibrationInspectionDetailRequestDto {

    private String instrumentName;

    private String capacity;

    private String serialNumber;

    private String calibrationCertificateNo;

    private String inspectionStatus;

    private String inspectionRemark;
}
