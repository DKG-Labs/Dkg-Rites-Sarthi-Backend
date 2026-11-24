package com.sarthi.Sms.entity.qct;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "qct_duty")
public class QctDutyEntity {

    @Id
    @Column(name = "duty_id", nullable = false)
    private String dutyId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "shift", nullable = false)
    private String shift;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "shift_remarks")
    private String shiftRemarks;
}
