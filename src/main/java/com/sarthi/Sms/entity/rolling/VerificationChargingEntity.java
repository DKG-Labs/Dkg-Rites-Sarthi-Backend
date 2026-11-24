package com.sarthi.Sms.entity.rolling;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "rolling_verification_charging_detail")
public class VerificationChargingEntity {

    @Column(name = "duty_id", nullable = false)
    private String dutyId;

    @Id
    @Column(name = "heat_number", nullable = false)
    private String heatNo;

    @Column(name = "length", precision = 10, scale = 2, nullable = false)
    private BigDecimal length;

    @Column(name = "defect")
    private String defect;

    @Column(name = "bloom_tracking_verification", nullable = false)
    private Boolean bloomTrackingVerification = false;

    @Column(name = "hot_blooms_descaling", nullable = false)
    private Boolean hotBloomsDescaling = false;

    @Column(name="remarks")
    private String remarks;
}
