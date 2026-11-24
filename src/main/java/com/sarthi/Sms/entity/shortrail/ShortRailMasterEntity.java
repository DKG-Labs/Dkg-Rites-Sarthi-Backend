package com.sarthi.Sms.entity.shortrail;



import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "short_rail_master")
public class ShortRailMasterEntity {

    @EmbeddedId
    private ShortRailMasterKey id;

    // @Column(name = "Rail_Section_Inspected", nullable = false)
    // private String railSectionInspected;

    // @Column(name = "Rail_Grade_Inspected", nullable = false)
    // private String railGradeInspected;

    // @Column(name = "Acceptance_26m_CL_A")
    // private Integer acceptance26mClA;

    // @Column(name = "Acceptance_26m_CL_A_0_1")
    // private Integer acceptance26mClA01;

    // @Column(name = "Acceptance_26m_CL_B")
    // private Integer acceptance26mClB;

    // @Column(name = "Acceptance_26m_IU")
    // private Integer acceptance26mIu;

    // @Column(name = "Acceptance_25m_CL_A")
    // private Integer acceptance25mClA;

    // @Column(name = "Acceptance_25m_CL_A_0_1")
    // private Integer acceptance25mClA01;

    // @Column(name = "Acceptance_25m_CL_B")
    // private Integer acceptance25mClB;

    // @Column(name = "Acceptance_25m_IU")
    // private Integer acceptance25mIu;

    // @Column(name = "Acceptance_24m_CL_A")
    // private Integer acceptance24mClA;

    // @Column(name = "Acceptance_24m_CL_A_0_1")
    // private Integer acceptance24mClA01;

    // @Column(name = "Acceptance_24m_CL_B")
    // private Integer acceptance24mClB;

    // @Column(name = "Acceptance_24m_IU")
    // private Integer acceptance24mIu;

    // @Column(name = "Acceptance_13m_CL_A")
    // private Integer acceptance13mClA;

    // @Column(name = "Acceptance_13m_CL_A_0_1")
    // private Integer acceptance13mClA01;

    // @Column(name = "Acceptance_13m_CL_B")
    // private Integer acceptance13mClB;

    // @Column(name = "Acceptance_13m_IU")
    // private Integer acceptance13mIu;

    // @Column(name = "Acceptance_12m_CL_A")
    // private Integer acceptance12mClA;

    // @Column(name = "Acceptance_12m_CL_A_0_1")
    // private Integer acceptance12mClA01;

    // @Column(name = "Acceptance_12m_CL_B")
    // private Integer acceptance12mClB;

    // @Column(name = "Acceptance_12m_IU")
    // private Integer acceptance12mIu;

    // @Column(name = "Acceptance_11m_CL_A")
    // private Integer acceptance11mClA;

    // @Column(name = "Acceptance_11m_CL_A_0_1")
    // private Integer acceptance11mClA01;

    // @Column(name = "Acceptance_11m_CL_B")
    // private Integer acceptance11mClB;

    // @Column(name = "Acceptance_11m_IU")
    // private Integer acceptance11mIu;

    // @Column(name = "Acceptance_10m_CL_A")
    // private Integer acceptance10mClA;

    // @Column(name = "Acceptance_10m_CL_A_0_1")
    // private Integer acceptance10mClA01;

    // @Column(name = "Acceptance_10m_CL_B")
    // private Integer acceptance10mClB;

    // @Column(name = "Acceptance_10m_IU")
    // private Integer acceptance10mIu;

    // @Column(name = "Rejection_26m")
    // private Integer rejection26m;

    // @Column(name = "Rejection_13m")
    // private Integer rejection13m;

    // @Column(name = "Cutbart_26m")
    // private Integer cutbart26m;

    // @Column(name = "Cutbart_13m")
    // private Integer cutbart13m;

    // @Column(name = "Refinish_26m")
    // private Integer refinish26m;

    // @Column(name = "Refinish_13m")
    // private Integer refinish13m;

    // @Column(name = "Utnp_Rejection_26m")
    // private Integer utnprejection26m;

    // @Column(name = "Utnp_Rejection_13m")
    // private Integer utnprejection13m;

    // @Column(name = "Utnp_Cutbart_26m")
    // private Integer utnpcutbart26m;

    // @Column(name = "Utnp_Cutbart_13m")
    // private Integer utnpcutbart13m;

    // @Column(name = "Utnp_Refinish_26m")
    // private Integer utnprefinish26m;

    // @Column(name = "Utnp_Refinish_13m")
    // private Integer utnprefinish13m;

    // @Column(name="created_at")
    // private LocalDateTime createdAt;

    // @Column(name = "Remarks")
    // private String remarks;

    @Column(name = "Acceptance_26m_CL_A")
    private Integer acceptance26mClA;

    @Column(name = "Acceptance_26m_CL_A_0_1")
    private Integer acceptance26mClA01;

    @Column(name = "Acceptance_26m_CL_B")
    private Integer acceptance26mClB;

    @Column(name = "Acceptance_26m_IU")
    private Integer acceptance26mIu;

    @Column(name = "Acceptance_25m_CL_A")
    private Integer acceptance25mClA;

    @Column(name = "Acceptance_25m_CL_A_0_1")
    private Integer acceptance25mClA01;

    @Column(name = "Acceptance_25m_CL_B")
    private Integer acceptance25mClB;

    @Column(name = "Acceptance_25m_IU")
    private Integer acceptance25mIu;

    @Column(name = "Acceptance_24m_CL_A")
    private Integer acceptance24mClA;

    @Column(name = "Acceptance_24m_CL_A_0_1")
    private Integer acceptance24mClA01;

    @Column(name = "Acceptance_24m_CL_B")
    private Integer acceptance24mClB;

    @Column(name = "Acceptance_24m_IU")
    private Integer acceptance24mIu;

    @Column(name = "Acceptance_13m_CL_A")
    private Integer acceptance13mClA;

    @Column(name = "Acceptance_13m_CL_A_0_1")
    private Integer acceptance13mClA01;

    @Column(name = "Acceptance_13m_CL_B")
    private Integer acceptance13mClB;

    @Column(name = "Acceptance_13m_IU")
    private Integer acceptance13mIu;

    @Column(name = "Acceptance_12m_CL_A")
    private Integer acceptance12mClA;

    @Column(name = "Acceptance_12m_CL_A_0_1")
    private Integer acceptance12mClA01;

    @Column(name = "Acceptance_12m_CL_B")
    private Integer acceptance12mClB;

    @Column(name = "Acceptance_12m_IU")
    private Integer acceptance12mIu;

    @Column(name = "Acceptance_11m_CL_A")
    private Integer acceptance11mClA;

    @Column(name = "Acceptance_11m_CL_A_0_1")
    private Integer acceptance11mClA01;

    @Column(name = "Acceptance_11m_CL_B")
    private Integer acceptance11mClB;

    @Column(name = "Acceptance_11m_IU")
    private Integer acceptance11mIu;

    @Column(name = "Acceptance_10m_CL_A")
    private Integer acceptance10mClA;

    @Column(name = "Acceptance_10m_CL_A_0_1")
    private Integer acceptance10mClA01;

    @Column(name = "Acceptance_10m_CL_B")
    private Integer acceptance10mClB;

    @Column(name = "Acceptance_10m_IU")
    private Integer acceptance10mIu;

    @Column(name = "rej_ref_rejection_26m")
    private Integer rejection26m;

    @Column(name = "rej_ref_cutbar_26m")
    private Integer cutbar26m;

    @Column(name = "rej_ref_refinish_26m")
    private Integer refinish26m;

    @Column(name = "rej_ref_rejection_13m")
    private Integer rejection13m;

    @Column(name = "rej_ref_cutbar_13m")
    private Integer cutbar13m;

    @Column(name = "rej_ref_refinish_13m")
    private Integer refinish13m;

    @Column(name = "utnp_rejection_26m")
    private Integer utnpRejection26m;

    @Column(name = "utnp_cutbar_26m")
    private Integer utnpCutbar26m;

    @Column(name = "utnp_refinish_26m")
    private Integer utnpRefinish26m;

    @Column(name = "utnp_rejection_13m")
    private Integer utnpRejection13m;

    @Column(name = "utnp_cutbar_13m")
    private Integer utnpCutbar13m;

    @Column(name = "utnp_refinish_13m")
    private Integer utnpRefinish13m;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "Remarks", length = 50)
    private String remarks;



}
