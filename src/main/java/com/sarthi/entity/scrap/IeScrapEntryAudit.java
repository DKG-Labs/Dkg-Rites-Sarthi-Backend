package com.sarthi.entity.scrap;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "ie_scrap_entry_audit")
@Data
public class IeScrapEntryAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "scrap_entry_id", nullable = false)
    private Integer scrapEntryId;

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
    private LocalDateTime actionAt = LocalDateTime.now();
}
