package com.sarthi.Sms.entity.rolling;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="rolling_test_sample_acceptance_relation")
public class AcptTestRelationEntity {
    
    @EmbeddedId
    private AcptTestRelationId atri;

    @Column(name="tests_marked")
    private String testsMarked;
}
