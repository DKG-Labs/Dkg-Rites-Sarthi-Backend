package com.sarthi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * One row per UDM rejection sync run (daily scheduler or Manual Sync):
 * when it ran, who/what triggered it, what CRIS returned and, if it failed, why.
 */
@Data
@Entity
@Table(name = "udm_sync_log", indexes = {
        @Index(name = "idx_udm_sync_started", columnList = "started_at")
})
public class UdmSyncLog {

    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_NO_DATA = "NO_DATA";
    public static final String STATUS_PARTIAL = "PARTIAL";   // some records could not be saved
    public static final String STATUS_FAILED = "FAILED";

    public static final String TRIGGER_SCHEDULER = "SCHEDULER";
    public static final String TRIGGER_MANUAL = "MANUAL";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The rejection date that was requested from CRIS */
    @Column(name = "sync_date", nullable = false)
    private LocalDate syncDate;

    /** SCHEDULER or MANUAL */
    @Column(name = "trigger_type", length = 20, nullable = false)
    private String triggerType;

    /** User who pressed Manual Sync (null for the scheduler) */
    @Column(name = "triggered_by", length = 150)
    private String triggeredBy;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    /** RUNNING, SUCCESS, NO_DATA, PARTIAL or FAILED */
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    /** Records returned by CRIS */
    @Column(name = "records_received")
    private Integer recordsReceived;

    /** Rejections stored for the first time */
    @Column(name = "records_new")
    private Integer recordsNew;

    /** Rejections already stored and refreshed */
    @Column(name = "records_updated")
    private Integer recordsUpdated;

    /** Records skipped (no rejection number) or that failed to save */
    @Column(name = "records_failed")
    private Integer recordsFailed;

    /** Why the run failed or was partial (CRIS error, login failure, save error...) */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
