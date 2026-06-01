package com.sarthi.dto.Calibration;


import lombok.Data;

import java.time.LocalDate;

@Data
public class IeVendorCalibrationInspectionDetailResponseDto {

    private Long id;

    private String instrumentName;

    private String capacity;

    private String description;

    private String usedFor;

    private String serialNumber;

    private String calibrationCertificateNo;

    private LocalDate calibrationDate;

    private LocalDate calibrationDueDate;

    private String certifyingLabName;

    private String accreditationAgency;

    private Integer notificationDays;

    private String calibrationStatus;

    private String inspectionStatus;

    private String inspectionRemark;
}