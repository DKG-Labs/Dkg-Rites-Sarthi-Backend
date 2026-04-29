package com.sarthi.Sleeper.entity.FInalCall;

import com.sarthi.Sleeper.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "sleeper_schedule")
@Data
public class SleeperSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_no", nullable = false)
    private String callNo;

    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;

    @Column(name = "reason", length = 500)
    private String reason;
}
