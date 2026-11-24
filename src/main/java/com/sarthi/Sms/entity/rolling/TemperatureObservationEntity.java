package com.sarthi.Sms.entity.rolling;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="rolling_temperature_observation")
public class TemperatureObservationEntity {
    @EmbeddedId
    private TemperatureObservationId id;

    @Column(name = "soaking_zone_temperature")
    private Integer soakingZoneTemp;

    @Column(name = "finishing_temperature")
    private Integer finishingTemp;

    @Column(name="remarks")
    private String remarks;
}
