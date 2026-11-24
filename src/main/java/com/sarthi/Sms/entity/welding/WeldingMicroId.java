package com.sarthi.Sms.entity.welding;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeldingMicroId implements Serializable{
     @Column(name = "test_type", nullable = false)
    private String testType;

    @Column(name = "joint_number", nullable = false)
    private String jointNo;
}
