package com.sarthi.Sms.entity.ndt;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "ndt_duty")
public class NdtDutyEntity {

    @Id
    @Column(name = "duty_id")
    private String dutyId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "shift", nullable = false, length = 10)
    private String shift;

    @Column(name = "mill", nullable = false, length = 10)
    private String mill;

    @Column(name = "organisation", length = 20)
    private String organisation;

    @Column(name = "ndt", nullable = false, length = 50)
    private String ndt;

    @Column(name = "rail_grade", nullable = false, length = 10)
    private String railGrade;

    @Column(name = "rail_section", nullable = false, length = 10)
    private String railSection;

    @Column(name = "shift_remarks", length = 50)
    private String shiftRemarks;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name= "calibration_remarks")
    private String calibrationremarks;
}
