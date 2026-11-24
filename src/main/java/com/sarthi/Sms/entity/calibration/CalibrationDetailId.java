package com.sarthi.Sms.entity.calibration;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class CalibrationDetailId implements Serializable {

    @Column(name = "serial_number", nullable = false)
    private String serialNumber;

    @Column(name = "duty_id", nullable = false)
    private String dutyId;
}
