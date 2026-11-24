package com.sarthi.Sms.dto.rolling;

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
public class BloomVerificationId implements Serializable{
    @Column(name = "heat_number", nullable = false)
    private String heatNo;

    @Column(name = "duty_id", nullable = false)
    private String dutyId;
}
