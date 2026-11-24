package com.sarthi.Sms.entity.qct;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name="qct_center_line_master")
@Entity
public class QctCenterLineMasterEntity {
    @Id
    private Integer qctId;

    @Column(name="duty_id")
    private String dutyId;

    @Column(name = "remarks")
    private String remarks;

    @Column(name="mean_rsh")
    private BigDecimal meanRsh;

    @Column(name="mean_variation")
    private BigDecimal maxVariation;

    @Column(name="hardness_reading_count")
    private Integer hardnessReadingCount;
}
