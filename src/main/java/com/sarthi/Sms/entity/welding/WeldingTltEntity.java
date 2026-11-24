package com.sarthi.Sms.entity.welding;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "welding_tlt")
public class WeldingTltEntity {

    @EmbeddedId
    private WeldingTltKey id;

    @Column(name = "machine_number", nullable = false)
    private String machineNo;

    @Column(name = "span", nullable = false)
    private BigDecimal span;

    @Column(name = "load_applied", nullable = false)
    private BigDecimal loadApplied;

    @Column(name = "deflection")
    private BigDecimal deflection;

    @Column(name = "load_at_20_mm_deflection", nullable = false)
    private BigDecimal loadAt20MmDeflection;

    @Column(name = "is_broken")
    private Boolean isBroken;

    @Column(name = "remark", nullable = false)
    private String remark;

    @Column(name = "duty_id", nullable = false)
    private String dutyId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


}
