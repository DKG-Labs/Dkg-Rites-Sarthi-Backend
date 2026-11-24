package com.sarthi.Sms.entity.rolling;

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
public class AcptTestRelationId implements Serializable {
    @Column(name="duty_id", nullable = false)
    private String dutyId;

    @Column(name="rolling_test_sample_id", nullable = false)
    private Integer rollingAcptTestId;
}
