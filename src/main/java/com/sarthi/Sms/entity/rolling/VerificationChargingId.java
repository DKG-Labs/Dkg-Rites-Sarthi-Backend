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
public class VerificationChargingId implements Serializable {
    @Column(name = "duty_id", nullable = false)
        private String dutyId;

    @Column(name = "heat_number", nullable = false)
    private String heatNo;
}
