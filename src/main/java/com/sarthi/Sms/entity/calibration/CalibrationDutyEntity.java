package com.sarthi.Sms.entity.calibration;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "calibration_duty")
public class CalibrationDutyEntity {

    @Id
    @Column(name = "duty_id", nullable = false, length = 255)
    private String dutyId;

    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    @Column(name = "shift", nullable = false, length = 10)
    private String shift;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "shift_remarks", length = 50)
    private String shiftRemarks;
}
