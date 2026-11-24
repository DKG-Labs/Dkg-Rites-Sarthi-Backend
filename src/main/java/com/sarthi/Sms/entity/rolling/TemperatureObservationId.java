package com.sarthi.Sms.entity.rolling;

import java.io.Serializable;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemperatureObservationId implements Serializable {
    public TemperatureObservationId(String dutyId2, String convertStringToTimeObj) {
        //TODO Auto-generated constructor stub
    }

    @Column(name = "duty_id", nullable = false)
        private String dutyId;

    @Column(name = "created_at", nullable = false)
    private LocalTime createdAt;
}
