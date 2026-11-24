package com.sarthi.Sms.entity.qct;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="qct_residual_master")
@Data
public class QctResidualMasterEntity {
    @Id
    @Column(name = "duty_Id")
    private String dutyId;  // Note: Changed to String to match VARCHAR

    @Column(name = "qct_id")
    private Integer qctId;

    @Column(name = "stress")
    private BigDecimal stress;

    @Column(name = "stress_nature")
    private String stressNature;

    @Column(name = "remarks")
    private String remarks;
}
