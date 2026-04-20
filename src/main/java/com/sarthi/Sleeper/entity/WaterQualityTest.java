package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "water_quality_test")
@Data
public class WaterQualityTest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "test_date")
    private LocalDate testDate;

    @Column(name = "ph_value")
    private Double phValue;

    @Column(name = "tds_result")
    private Double tdsResult;

    @Column(name = "result")
    private String result;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
