package com.sarthi.Sms.entity.welding;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "welding_inspection_detail")
@Data
public class WeldingInspectionDetailEntity {

    @Id
    @Column(name = "joint_number")
    private String jointNumber;

    @Column(name = "inspection_master_id", nullable = false)
    private Integer inspectionMasterId;

    @Column(name = "weld_parameter")
    private Boolean weldParameter;

    @Column(name = "marking")
    private Boolean marking;

    @Column(name="visual")
    private Boolean visual;

    @Column(name = "dimension")
    private Boolean dimension;

    @Column(name = "dimension_detail")
    private String dimensionDetail;

    @Column(name = "usfd")
    private Boolean usfd;

    @Column(name = "usfd_detail")
    private String usfdDetail;

    @Column(name = "weld_operator")
    private String weldOperator;

    @Column(name = "usfd_operator")
    private String usfdOperator;

    @Column(name = "result")
    private String result;

    @Column(name = "rail_id_1", nullable = false)
    private String railId1;

    @Column(name = "rail_id_2", nullable = false)
    private String railId2;

    @Column(name = "remark")
    private String remark;
}