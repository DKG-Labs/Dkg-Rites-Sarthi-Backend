package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "moment_of_resistance_detail")
@Data
public class MomentOfResistanceDetail {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "mr_test_id")
        private MomentOfResistanceTest mrTest;

        // Type → SCADA / MANUAL
        @Column(name = "data_type")
        private String dataType;

        // Values
        @Column(name = "ct")
        private Double ct;

        @Column(name = "cb")
        private Double cb;

        @Column(name = "rs")
        private Double rs;

}
