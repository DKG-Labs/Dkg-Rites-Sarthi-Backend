package com.sarthi.SRailPad.entity.ieVerification;

import com.sarthi.SRailPad.entity.BaseEntity;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import jakarta.persistence.*;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Entity
@Table(name = "rail_hydraulic_press")
public class RailHydraulicPress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "casting_date")
    private LocalDate castingDate;

    @Column(name = "rail_pad_type", nullable = false)
    private String railPadType;

    @Column(name = "batch_no", nullable = false)
    private String batchNo;

    @Column(name = "time_of_check", nullable = false)
    private String timeOfCheck;

    @Column(name = "curing_time", nullable = false)
    private Double curingTime;

    @Column(name = "curing_temp", nullable = false)
    private Double curingTemp;

    @Column(name = "curing_pressure", nullable = false)
    private Double curingPressure;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "timestamp", nullable = false)
    private String timestamp;

    @PrePersist
    protected void onCreate() {
        setCreatedDate(java.time.LocalDateTime.now());
        setUpdatedDate(java.time.LocalDateTime.now());
    }

    @PreUpdate
    protected void onUpdate() {
        setUpdatedDate(java.time.LocalDateTime.now());
    }
}
