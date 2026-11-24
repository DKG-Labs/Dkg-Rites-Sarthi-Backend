package com.sarthi.Sms.entity.rolling;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "ht_sequence")
@Data
public class HtSequenceEntity {

    @Id
    @Column(name = "rail_id")
    private String railId;

    @Enumerated(EnumType.STRING)
    @Column(name = "bloom_quality")
    private BloomQuality bloomQuality;

    @Column(name = "ht_status")
    private Boolean htStatus;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "batch_open")
    private Boolean batchOpen = true;

    @Column(name = "batch_end_duty_id")
    private String batchEndDutyId;
    
    // Enum for Bloom Quality
    public enum BloomQuality {
        CC, CO
    }
}

