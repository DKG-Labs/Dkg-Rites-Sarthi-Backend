package com.sarthi.Sms.entity.welding;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "welding_inspection_master")
@Data
public class WeldingMasterInspectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inspection_master_id")
    private Integer inspectionMasterId;

    @Column(name = "panel_id", nullable = false)
    private String panelId;

    @Column(name = "number_of_joints", nullable = false)
    private Integer numberOfJoints;

    @Column(name = "panel_length", nullable = false)
    private Integer panelLength;

    @Column(name = "panel_decision", nullable = false)
    private String panelDecision;

    @Column(name = "panel_remark", nullable = false)
    private String panelRemark;

    @Column(name = "front_result")
    private Boolean frontResult;

    @Column(name = "back_result")
    private Boolean backResult;

    @Column(name = "front_result_reason_for_not_ok")
    private String frontResultReasonForNotOk;

    @Column(name = "back_result_reason_for_not_ok")
    private String backResultReasonForNotOk;

    @Column(name = "front_remark")
    private String frontRemark;

    @Column(name = "back_remark")
    private String backRemark;
}
