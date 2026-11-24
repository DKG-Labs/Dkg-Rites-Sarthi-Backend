package com.sarthi.Sms.entity.rolling;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "retest_sample")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetestSample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "retest_sample_id")
    private Integer retestSampleId;

    private String railGrade;

    private String dutyId;

    private String sampleId;

    private String sampleLot;

    private String heatNumber;

    private String strandNumber;

    private String testType;

    @Column(name= "create_date")
    private LocalDateTime createDate = LocalDateTime.now();
}