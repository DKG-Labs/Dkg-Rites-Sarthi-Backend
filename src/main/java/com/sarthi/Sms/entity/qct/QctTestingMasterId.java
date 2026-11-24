package com.sarthi.Sms.entity.qct;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Data
@Embeddable
public class QctTestingMasterId implements Serializable {

    @Column(name = "duty_id", nullable = false)
    private String dutyId;

    @Column(name = "qct_type", nullable = false)
    private String qctType;
}
