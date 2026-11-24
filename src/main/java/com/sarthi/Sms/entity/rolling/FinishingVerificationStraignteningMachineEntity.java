package com.sarthi.Sms.entity.rolling;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="rolling_finishing_verification_straightening_machine")
public class FinishingVerificationStraignteningMachineEntity {
    @Id
    @Column(name = "duty_id", nullable = false, length = 50)
    private String dutyId;

    @Column(name = "sm_r1_hrsm", length = 50)
    private String smR1Hrsm;

    @Column(name = "sm_r4_hrsm", length = 50)
    private String smR4Hrsm;

    @Column(name = "sm_r6_hrsm", length = 50)
    private String smR6Hrsm;

    @Column(name = "sm_r8_hrsm", length = 50)
    private String smR8Hrsm;

    @Column(name = "sm_r1_vrsm", length = 50)
    private String smR1Vrsm;

    @Column(name = "sm_r4_vrsm", length = 50)
    private String smR4Vrsm;

    @Column(name = "sm_r6_vrsm", length = 50)
    private String smR6Vrsm;

    @Column(name = "sm_r8_vrsm", length = 50)
    private String smR8Vrsm;

    @Column(name = "sm_is_satisfactory")
    private Boolean smIsSatisfactory;

    @Column(name = "sm_sr_r1_hrsm", length = 50)
    private String smSrR1Hrsm;

    @Column(name = "sm_sr_r2_hrsm", length = 50)
    private String smSrR2Hrsm;

    @Column(name = "sm_sr_r3_hrsm", length = 50)
    private String smSrR3Hrsm;

    @Column(name = "sm_sr_r4_hrsm", length = 50)
    private String smSrR4Hrsm;

    @Column(name = "sm_sr_r1_vrsm", length = 50)
    private String smSrR1Vrsm;

    @Column(name = "sm_sr_r2_vrsm", length = 50)
    private String smSrR2Vrsm;

    @Column(name = "sm_sr_r3_vrsm", length = 50)
    private String smSrR3Vrsm;

    @Column(name = "sm_sr_r4_vrsm", length = 50)
    private String smSrR4Vrsm;

    @Column(name = "sm_sr_is_satisfactory")
    private Boolean smSrIsSatisfactory;

    @Column(name = "sm_lr_r1_hrsm", length = 50)
    private String smLrR1Hrsm;

    @Column(name = "sm_lr_r2_hrsm", length = 50)
    private String smLrR2Hrsm;

    @Column(name = "sm_lr_r3_hrsm", length = 50)
    private String smLrR3Hrsm;

    @Column(name = "sm_lr_r4_hrsm", length = 50)
    private String smLrR4Hrsm;

    @Column(name = "sm_lr_r1_vrsm", length = 50)
    private String smLrR1Vrsm;

    @Column(name = "sm_lr_r2_vrsm", length = 50)
    private String smLrR2Vrsm;

    @Column(name = "sm_lr_r3_vrsm", length = 50)
    private String smLrR3Vrsm;

    @Column(name = "sm_lr_r4_vrsm", length = 50)
    private String smLrR4Vrsm;

    @Column(name = "sm_lr_is_satisfactory")
    private Boolean smLrIsSatisfactory;

    @Column(name = "straigtness_remarks", length = 50)
    private String straigtnessRemarks;

    @Column(name = "remarks", length = 50)
    private String remarks;
}
