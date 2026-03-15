package com.sarthi.Sleeper.entity.Aggregate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "aggregate_flakiness_row")
public class AggregateFlakinessRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "flakiness_test_id")
    @JsonIgnore
    private AggregateFlakinessTest flakinessTest;

    private String category; // e.g. "10mm", "20mm"
    private Double passingSize;
    private Double retainedSize;
    private Double weightSampleA;
    private Double weightPassedB;
    private Double weightRetainedC;
    private Double weightRetainedLengthD;
}
