package com.sarthi.Sms.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DutyEnum {

    SMS("SMS", "SMS"),
    VISUAL_INSPECTION("VIN", "Visual Inspection"),
    ROLLING("RLG", "Rolling Stage"),
    TESTING("TST", "Testing"),
    NDT("NDT", "NDT"),
    WLD("WLD", "Welding"),
    CLB("CLB", "Calibration"),
    QCT("QCT", "QCT"),
    SRI("SRI", "SRI");

    private final String code;
    private final String description;

}
