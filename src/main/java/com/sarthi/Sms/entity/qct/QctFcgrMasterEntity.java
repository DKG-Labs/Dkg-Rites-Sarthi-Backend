package com.sarthi.Sms.entity.qct;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="qct_fcgr_master")
@Data
public class QctFcgrMasterEntity {
    @Id
    private Integer qctId;

    @Column(name="duty_id")
    private String qctDuty;

    @Column(name = "mpam_10")
    private BigDecimal mpam10;

    @Column(name = "mpam_13")
    private BigDecimal mpam13;

    @Column(name = "remarks")
    private String remarks;
}
