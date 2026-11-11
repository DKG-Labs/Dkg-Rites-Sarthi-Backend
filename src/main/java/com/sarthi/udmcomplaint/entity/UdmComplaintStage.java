package com.sarthi.udmcomplaint.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** One Controlling Manager stage submission (decision, JI, audit, RCA, CAPA). */
@Entity
@Table(name = "udm_complaint_stage")
@Data
public class UdmComplaintStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "complaint_id", nullable = false)
    private Long complaintId;

    @Column(name = "udm_rejection_id", nullable = false)
    private Long udmRejectionId;

    @Column(name = "stage", nullable = false, length = 30)
    private String stage;

    @Column(name = "round_no", nullable = false)
    private Integer roundNo;

    @Column(name = "path", length = 10)
    private String path;

    @Column(name = "flag_failed_90")
    private Boolean flagFailed90;

    @Column(name = "flag_qty_over_5")
    private Boolean flagQtyOver5;

    @Column(name = "flag_critical")
    private Boolean flagCritical;

    @Column(name = "stage_date")
    private LocalDate stageDate;

    @Column(name = "ji_team", length = 1000)
    private String jiTeam;

    /** Comma-separated user ids of SARTHI members in the JI team (V20261111_004). */
    @Column(name = "ji_team_user_ids", length = 500)
    private String jiTeamUserIds;

    @Column(name = "ji_outcome", length = 60)
    private String jiOutcome;

    @Column(name = "audit_type", length = 40)
    private String auditType;

    @Column(name = "findings", length = 1000)
    private String findings;

    @Column(name = "attributable_to", length = 60)
    private String attributableTo;

    @Column(name = "root_cause", length = 60)
    private String rootCause;

    @Column(name = "corrective_actions", length = 600)
    private String correctiveActions;

    @Column(name = "preventive_actions", length = 600)
    private String preventiveActions;

    @Column(name = "vendor_compliance", length = 1)
    private String vendorCompliance;

    @Column(name = "capa_implemented", length = 1)
    private String capaImplemented;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
