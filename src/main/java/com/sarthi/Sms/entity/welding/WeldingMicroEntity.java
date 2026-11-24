package com.sarthi.Sms.entity.welding;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "welding_micro")
public class WeldingMicroEntity {

    @EmbeddedId
    private WeldingMicroId id;

    @Column(name = "is_micro_free")
    private Boolean isMicroFree;

    @Column(name = "is_astm_coarser")
    private Boolean isAstmCoarser;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "duty_id", nullable = false)
    private String dutyId;   // No annotations, simple variable

    @Column(name = "machine_number")
    private String machineNo; // Converted number to 'no'

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
