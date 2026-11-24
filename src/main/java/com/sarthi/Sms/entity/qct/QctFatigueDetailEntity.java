package com.sarthi.Sms.entity.qct;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name="qct_fatigue_detail")
@Entity
public class QctFatigueDetailEntity {
    @Id
    @Column(name = "qct_id")
    private Integer qctId;

    @Column(name = "duty_Id", nullable = false)
    private String dutyId;

    @Column(name = "is_broken")
    private Boolean isBroken;
}
