package com.sarthi.Sms.entity.rolling;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="rolling_duty")
public class RollingDutyEntity {
    @Id
    @Column(name = "duty_id", length = 50, nullable = false)
    private String dutyId;

    @Column(name = "user_id", length = 50, nullable = false)
    private String userId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "shift", length = 10, nullable = false)
    private String shift;

    @Column(name = "mill", length = 10, nullable = false)
    private String mill;

    @Column(name = "organisation", length = 20)
    private String organisation;

    @Column(name = "rail_grade", length = 50, nullable = false)
    private String railGrade;

    @Column(name = "rail_section", length = 50, nullable = false)
    private String railSection;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name="vernier_number")
    private String vernierNo;

    @Column(name="micrometer_number")
    private String micrometerNo;

    @Column(name="weighing_machine")
    private String weighingMachine;

    @Column(name="branding")
    private String branding;

    @Column(name="number_of_gauges")
    private Integer noOfGauges;

    @Column(name="calibration_validity_vernier")
    private LocalDate vernierValidity;

    @Column(name="calibration_validity_micrometer")
    private LocalDate micrometerValidity;

    @Column(name="calibration_validity_weighing_machine")
    private LocalDate weighingMachineValidity;

    @Column(name = "shift_remarks", length = 50)
    private String shiftRemarks;
}
