package com.sarthi.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class VendorCalibrationDetailDto {
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
}
