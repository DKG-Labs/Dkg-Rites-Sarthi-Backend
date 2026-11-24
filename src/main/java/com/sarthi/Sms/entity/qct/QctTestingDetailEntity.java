package com.sarthi.Sms.entity.qct;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

@Data
@Entity
@Table(name = "qct_testing_detail_table")
public class QctTestingDetailEntity {

    @EmbeddedId
    private QctTestingDetailId id;

    @Column(name = "duty_id", nullable = false)
    private String dutyId;

    @Column(name = "fatique_result")
    private String fatiqueResult;

    @Column(name = "stress_mpa", nullable = false)
    private Float stressMpa;

    @Column(name = "nature_of_stress")
    private String natureOfStress;

    @Column(name = "fcgr_da_dn_10")
    private Float fcgrDaDn10;

    @Column(name = "fcgr_da_dn_13_5")
    private Float fcgrDaDn13_5;

    @Column(name = "value_reported_as")
    private String valueReportedAs;

    @Column(name = "value_of_reported_parameter")
    private Float valueOfReportedParameter;

    @Column(name = "no_of_hardness")
    private Integer noOfHardness;

    @Column(name = "mean_rsh")
    private Float meanRsh;

    @Column(name = "mx_variation")
    private Float mxVariation;
}
