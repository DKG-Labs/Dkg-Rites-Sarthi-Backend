package com.sarthi.Sms.entity.sms;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name="sms_duty")
public class SmsDutyEntity {
    @Column(name = "user_id")
    private String userId;

    @Id
    @Column(name="duty_id")
    private String dutyId;

     @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "shift", nullable = false, length = 50)
    private String shift;

    @Column(name = "sms", nullable = false, length = 50)
    private String sms;

    @Column(name = "rail_grade", nullable = false, length = 50)
    private String railGrade;

    @Column(name = "shift_remarks", length = 100)
    private String shiftRemarks;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "is_ems_functioning", nullable = false)
    private boolean isEmsFunctioning = false;

    @Column(name = "is_slag_detector_functioning", nullable = false)
    private boolean isSlagDetectorFunctioning = false;

    @Column(name = "is_amlc_functioning", nullable = false)
    private boolean isAmlcFunctioning = false;

    @Column(name = "is_hydrogen_measurement_automatic", nullable = false)
    private boolean isHydrogenMeasurementAutomatic = false;

    @Column(name = "is_ladle_to_tundish_used", nullable = false)
    private boolean isLadleToTundishUsed = false;

    @Column(name = "is_tundish_to_mould_used", nullable = false)
    private boolean isTundishToMouldUsed = false;

    @Column(name = "make_of_casting_powder", length = 50)
    private String makeOfCastingPowder;

    @Column(name = "make_of_hydris_probe", length = 50)
    private String makeOfHydrisProbe;
}