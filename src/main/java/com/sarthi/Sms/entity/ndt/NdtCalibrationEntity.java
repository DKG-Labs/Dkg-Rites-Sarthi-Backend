package com.sarthi.Sms.entity.ndt;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Data
@Table(name = "ndt_calibration")
public class NdtCalibrationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calibration_id")
    private Integer calibrationId;

    @Column(name = "duty_id", nullable = false)
    private String dutyId;

    @Column(name = "ut", nullable = false)
    private Boolean ut = false;

    @Column(name = "ct", nullable = false)
    private Boolean ct = false;

    @Column(name = "fmg", nullable = false)
    private Boolean fmg = false;

    @Column(name = "OSIRIS", nullable = false)
    private Boolean osiris = false;

    @Column(name = "calibration_completion_time", nullable = false)
    private LocalTime calibrationCompletionTime;

    @Column(name = "calibration_speed", nullable = false)
    private BigDecimal calibrationSpeed;

    @Column(name = "rcl_rep_name", nullable = false, length = 50)
    private String rclRepName;

    @Column(name = "calibration_round_remarks", nullable = false, length = 50)
    private String calibrationRoundRemarks;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate = LocalDateTime.now();

}

