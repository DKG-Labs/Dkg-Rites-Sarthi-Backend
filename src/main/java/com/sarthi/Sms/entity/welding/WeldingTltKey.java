package com.sarthi.Sms.entity.welding;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Data
@Embeddable
public class WeldingTltKey implements Serializable {

    @Column(name = "test_type", nullable = false)
    private String testType;

    @Column(name = "joint_number", nullable = false)
    private String jointNumber;
}
