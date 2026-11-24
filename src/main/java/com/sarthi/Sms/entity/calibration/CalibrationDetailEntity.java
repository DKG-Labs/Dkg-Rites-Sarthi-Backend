package com.sarthi.Sms.entity.calibration;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "calibration_detail")
public class CalibrationDetailEntity {

    @EmbeddedId
    private CalibrationDetailId id;

    @Column(name = "instrument_category", nullable = false)
    private String instrumentCategory;

    @Column(name = "instrument", nullable = false)
    private String instrument;

    @Column(name = "detail", nullable = false)
    private String detail;

    @Column(name = "rail_section")
    private String railSection;

    @Column(name = "notification_after_days")
    private Integer notificationAfterDays;

    @Column(name = "calibration_date", nullable = false)
    private LocalDate calibrationDate;

    @Column(name = "result", nullable = false)
    private String result;

    @Column(name = "calibration_valid_upto")
    private LocalDate calibrationValidUpto;
}
