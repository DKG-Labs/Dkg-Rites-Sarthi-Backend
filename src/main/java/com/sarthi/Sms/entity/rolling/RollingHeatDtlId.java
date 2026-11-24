package com.sarthi.Sms.entity.rolling;

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
public class RollingHeatDtlId implements Serializable {
    @Column(name = "heat_number", nullable = false)
    private String heatNo;

    @Column(name = "sample_number", nullable = false)
    private Integer sampleNo;
}
