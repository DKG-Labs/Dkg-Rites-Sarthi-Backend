package com.sarthi.Sleeper.entity.scrap;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/** Immutable record of an edit or delete of a sleeper scrap verification. */
@Data
@Entity
@Table(name = "sleeper_scrap_verification_audit")
public class SleeperScrapVerificationAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scrap_verification_id", nullable = false)
    private Long scrapVerificationId;

    /** EDIT or DELETE */
    @Column(name = "action", nullable = false, length = 10)
    private String action;

    @Column(name = "field_changed", length = 100)
    private String fieldChanged;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "action_by", nullable = false, length = 50)
    private String actionBy;

    @Column(name = "action_at", nullable = false)
    private LocalDateTime actionAt;
}
