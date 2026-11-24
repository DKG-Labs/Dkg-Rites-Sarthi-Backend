package com.sarthi.Sms.entity.welding;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "welding_duty_inspection_relation")
@Data
public class WeldingDutyRelationEntity {

    @EmbeddedId
    WeldingDutyInspectionRelationKey id;

    @Column(name = "panel_decision", nullable = false)
    private String panelDecision;
}

