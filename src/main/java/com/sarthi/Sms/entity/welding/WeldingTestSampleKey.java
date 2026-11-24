package com.sarthi.Sms.entity.welding;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Data
@Embeddable
public class WeldingTestSampleKey implements Serializable {

    @Column(name = "test_type", nullable = false, length = 20)
    private String testType;

    @Column(name = "joint_number", nullable = false, length = 50)
    private String jointNo;
}