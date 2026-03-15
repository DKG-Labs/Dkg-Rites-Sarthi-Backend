package com.sarthi.Sleeper.entity.Aggregate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "aggregate_granulometric_row")
public class AggregateGranulometricRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "granulometric_test_id")
    @JsonIgnore
    private AggregateGranulometricTest granulometricTest;

    private String sectionType; // "CA1", "CA2", "FA"
    private String sieveSize;
    private Double wtRetained;
    private Double cummWtRetained;
    private Double pctRetained;
    private Double pctPassing;
}
