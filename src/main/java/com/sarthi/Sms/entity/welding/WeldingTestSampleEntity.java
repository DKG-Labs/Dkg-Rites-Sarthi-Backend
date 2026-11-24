package com.sarthi.Sms.entity.welding;

import jakarta.persistence.*;
import lombok.Data;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;


@Data
@Entity
@Table(name = "welding_test_sample")
public class WeldingTestSampleEntity {

    @EmbeddedId
    private WeldingTestSampleKey id;

    @Column(name = "rail_grade", nullable = false, length = 50)
    private String railGrade;

    @Column(name = "rail_section", nullable = false, length = 50)
    private String railSection;

    @Column(name = "plant", nullable = false, length = 20)
    private String plant;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "duty_id", nullable = false, length = 50)
    private String dutyId;
}

