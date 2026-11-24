package com.sarthi.Sms.entity.welding;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "welding_hardness")
public class WeldingHardnessEntity {

    @EmbeddedId
    private WeldingHardnessKey id;

    @Column(name = "machine_number", nullable = false)
    private String machineNo;

    @Column(name = "parent_hardness_l", nullable = false)
    private BigDecimal parentHardnessL;

    @Column(name = "parent_hardness_r", nullable = false)
    private BigDecimal parentHardnessR;

    @Column(name = "haz_hardness_l", nullable = false)
    private BigDecimal hazHardnessL;

    @Column(name = "haz_hardness_r", nullable = false)
    private BigDecimal hazHardnessR;

    @Column(name = "deviation_l", nullable = false)
    private BigDecimal deviationL;

    @Column(name = "deviation_r", nullable = false)
    private BigDecimal deviationR;

    @Column(name = "remark", nullable = false)
    private String remark;

    @Column(name = "duty_id", nullable = false)
    private String dutyId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


}
