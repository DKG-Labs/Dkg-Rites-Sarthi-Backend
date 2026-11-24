package com.sarthi.Sms.entity.rolling;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Table(name="ht_sequence_heat_rail")
@Entity
public class HtSequenceHeatRailEntity {
    @Id
    @Column(name = "rail_id")
    private String railId;

    @Column(name = "heat_number")
    private String heatNo;

    @Column(name = "bloom_quality")
    private String bloomQuality;

    @Column(name = "ht_status")
    private Boolean htStatus;

    @Column(name = "duty_id")
    private String dutyId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
