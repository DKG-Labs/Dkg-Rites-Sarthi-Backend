package com.sarthi.Sleeper.entity.Cement;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalTime;

@Entity
@Table(name = "cement_normal_consistency_observation")
@Data
public class CementNormalConsistencyObservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "percent_water_added")
    private Double percentWaterAdded;

    @Column(name = "volume")
    private Double volume;

    @Column(name = "time_of_adding")
    private LocalTime timeOfAdding;

    @Column(name = "reading_time")
    private LocalTime readingTime;

    @Column(name = "needle_reading")
    private Double needleReading;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cement_normal_consistency_id")
    private CementNormalConsistency cementNormalConsistency;
}
