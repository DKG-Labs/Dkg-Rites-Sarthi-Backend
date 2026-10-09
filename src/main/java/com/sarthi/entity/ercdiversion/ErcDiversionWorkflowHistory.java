package com.sarthi.entity.ercdiversion;

import com.sarthi.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "erc_diversion_workflow_history")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErcDiversionWorkflowHistory extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diversion_request_id", nullable = false)
    private ErcDiversionRequest diversionRequest;

    @Column(name = "action_role", nullable = false, length = 30)
    private String actionRole;

    @Column(name = "action_user_id", nullable = false, length = 50)
    private String actionUserId;

    @Column(name = "action_user_name", length = 100)
    private String actionUserName;

    @Column(name = "action", nullable = false, length = 30)
    private String action;

    @Column(name = "from_status", length = 30)
    private String fromStatus;

    @Column(name = "to_status", nullable = false, length = 30)
    private String toStatus;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
