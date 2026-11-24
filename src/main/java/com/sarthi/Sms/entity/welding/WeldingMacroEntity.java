package com.sarthi.Sms.entity.welding;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "welding_macro")
@Data
public class WeldingMacroEntity {

    @EmbeddedId
    private WeldingMacroId id;

    @Column(name = "head_l", precision = 10, scale = 2)
    private BigDecimal headL;

    @Column(name = "web_l", precision = 10, scale = 2)
    private BigDecimal webL;

    @Column(name = "foot_l", precision = 10, scale = 2)
    private BigDecimal footL;

    @Column(name = "head_r", precision = 10, scale = 2)
    private BigDecimal headR;

    @Column(name = "web_r", precision = 10, scale = 2)
    private BigDecimal webR;

    @Column(name = "foot_r", precision = 10, scale = 2)
    private BigDecimal footR;

    @Column(name = "remarks", length = 50)
    private String remarks;

    @Column(name = "duty_id", length = 50, nullable = false)
    private String dutyId;

    @Column(name = "machine_number", length = 50, nullable = false)
    private String machineNo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
