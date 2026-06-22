package com.sarthi.Sms.entity.sms;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Data
@Table(name="duty_heat_sms2")
public class DutyHeatSms2Entity {
    @EmbeddedId
    private DutyHeatRelationIdSms2 dutyHeatRelationId;

    @Column(name = "heat_procurement_stage", length = 50)
    private String heatProcurementStage;

    @Column(name = "heat_surrender_stage", length = 50)
    private String heatSurrenderStage;
}
