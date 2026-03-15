package com.sarthi.Sleeper.entity.Cement;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalTime;

@Entity
@Table(name = "cement_setting_time_observation")
@Data
public class CementSettingTimeObservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reading_time")
    private LocalTime readingTime;

    @Column(name = "needle_penetration")
    private Double needlePenetration;

    @Column(name = "final_spot")
    private String finalSpot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cement_setting_time_id")
    private CementSettingTime cementSettingTime;
}
