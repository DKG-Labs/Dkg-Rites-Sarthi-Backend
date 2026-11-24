package com.sarthi.Sms.dto.calibration;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Data
public class BulkCalibrationReqDto {
    private String detail;
    private String dutyId;
    private String calibrationResult;
    private Integer calibrationExpiryNumberOfDays;
    private String calibrationValidUpto;
    private List<String> serialNumberList;
}
