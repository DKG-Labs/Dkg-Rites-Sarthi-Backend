package com.sarthi.SRailPad.entity.ieVerification;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "rail_final_weight_test_sample")
public class RailFinalWeightTestSample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rail_final_weight_test_id", nullable = false)
    private RailFinalWeightTest railFinalWeightTest;

    @Column(name = "sampling_no", nullable = false)
    private Integer samplingNo;

    @Column(name = "sample_no", nullable = false)
    private Integer sampleNo;

    @Column(name = "sample_value", nullable = false)
    private Double sampleValue;

    @Column(name = "is_rejected", nullable = false)
    private Boolean isRejected;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }
}
