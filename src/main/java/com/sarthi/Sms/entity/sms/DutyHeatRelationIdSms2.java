package com.sarthi.Sms.entity.sms;

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
public class DutyHeatRelationIdSms2 implements Serializable {
    @Column(name="duty_id")
    private String dutyId;

    @Column(name="heat_number")
    private String heatNo;
}
