package com.sarthi.Sms.entity.rolling;

import java.math.BigDecimal;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
@Entity
@Data
@Table(name="rolling_control_heat_detail")
public class RollingHeatDtlEntity {
    
    @EmbeddedId
    private RollingHeatDtlId rollingHeatDtlId;

    @Column(name="duty_id")
    private String dutyId;

    @Column(name = "timing", nullable = false)
    private LocalTime timing;

    @Column(name = "sample_location", length = 50, nullable = false)
    private String sampleLocation;

    @Column(name = "height", precision = 10, scale = 2, nullable = false)
    private BigDecimal height;

    @Column(name = "head", precision = 10, scale = 5, nullable = false)
    private String head;

    @Column(name = "flange", precision = 10, scale = 5, nullable = false)
    private BigDecimal flange;

    @Column(name = "web", precision = 10, scale = 5, nullable = false)
    private BigDecimal web;

    @Column(name = "asy", length = 50)
    private String asy;

    @Column(name = "foot_toe", length = 50)
    private String footToe;

    @Column(name = "fishing_height", length = 50)
    private String fishingHeight;

    @Column(name = "weight", precision = 10, scale = 5)
    private BigDecimal weight;

    @Column(name = "crown_profile", length = 50)
    private String crownProfile;

    @Column(name = "foot_flatness", length = 50)
    private String footFlatness;

    @Column(name = "remark", length = 50)
    private String remark;

}
