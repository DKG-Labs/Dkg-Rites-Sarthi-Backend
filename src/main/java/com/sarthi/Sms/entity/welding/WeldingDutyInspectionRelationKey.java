package com.sarthi.Sms.entity.welding;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;
@Data
@Embeddable
public class WeldingDutyInspectionRelationKey implements Serializable {
    @Column(name = "duty_id")
    private String dutyId;

    @Column(name = "inspection_master_id")
    private Integer inspectionMasterId;
}
