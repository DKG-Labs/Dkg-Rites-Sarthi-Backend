package com.sarthi.Sms.entity.qct;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="qct_fracture_toughness_master")
public class QctFractureToughnessMasterEntity {
     @Id
    @Column(name = "qct_id")
    private int qctId;

    @Column(name = "duty_Id")
    private String dutyId;

    @Column(name = "parameter")
    private String parameter;

    @Column(name = "value")
    private BigDecimal value;

    @Column(name = "remarks")
    private String remarks;
}
