package com.sarthi.Sms.entity.testing;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="testing_duty")
public class TestingDutyEntity {
    
    @Id
    @Column(name="duty_id")
    private String dutyId;

    @Column(name="user_id")
    private String userId;

    @Column(name="date")
    private LocalDate date;

    @Column(name="start_time")
    private LocalDateTime startTime;

    @Column(name="end_time")
    private LocalDateTime endTime;

    @Column(name="shift")
    private String shift;

    @Column(name="shift_remarks")
    private String shiftRemarks;
}
