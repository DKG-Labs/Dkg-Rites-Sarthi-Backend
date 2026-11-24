package com.sarthi.Sms.dto.calibration;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CalibrationDetailDto {
    private String instrumentCategory;
    private String instrument;
    private String detail;
    private String railSection;
    private String serialNumber;
    private String calibrationValidUpto;
    private Integer notificationAfterDays;
    private String calibrationDate;
    private String result;
}
