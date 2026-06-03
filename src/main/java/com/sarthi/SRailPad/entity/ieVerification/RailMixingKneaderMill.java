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
@Table(name = "rail_mixing_kneader_mill")
public class RailMixingKneaderMill extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "casting_date")
    private LocalDate castingDate;

    @Column(name = "rail_pad_type", nullable = false)
    private String railPadType;

    @Column(name = "batch_no", nullable = false)
    private String batchNo;

    @Column(name = "mixing_time", nullable = false)
    private Double mixingTime;

    @Column(name = "mixing_temp", nullable = false)
    private Double mixingTemp;

    @Column(name = "water_circulation", nullable = false)
    private String waterCirculation;

    @Column(name = "dust_collector", nullable = false)
    private String dustCollector;

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
