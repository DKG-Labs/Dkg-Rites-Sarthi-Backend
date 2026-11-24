package com.sarthi.Sms.entity.rolling;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="rolling_finishing_verification_hot_stamping")
public class FinishingVerificationHotStampingEntity {
    @Id
    @Column(name = "heat_number", nullable = false, length = 50)
    private String heatNo;

    @Column(name = "brand", length = 50)
    private String brand;

    @Column(name = "duty_id", nullable = false, length = 50)
    private String dutyId;
}
