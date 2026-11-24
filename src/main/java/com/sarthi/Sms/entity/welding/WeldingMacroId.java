package com.sarthi.Sms.entity.welding;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class WeldingMacroId implements Serializable{
     @Column(name = "test_type", nullable = false)
    private String testType;

    @Column(name = "joint_number", nullable = false)
    private String jointNo;
}
