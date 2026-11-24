package com.sarthi.Sms.entity.qct;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "qct_fatigue_master")
@Data
public class QctFatigueMasterEntity {

    @Id
    @Column(name = "duty_Id", nullable = false)
    private String dutyId; // Primary Key as per your schema

    @Column(name = "strain")
    private BigDecimal strain;

    @Column(name = "frequency")
    private Integer frequency;

    @Column(name = "stress")
    private BigDecimal stress;

    @Column(name = "cycle_Endured")
    private BigDecimal cycleEndured;

    @Column(name = "remarks", length = 50)
    private String remarks;
}
