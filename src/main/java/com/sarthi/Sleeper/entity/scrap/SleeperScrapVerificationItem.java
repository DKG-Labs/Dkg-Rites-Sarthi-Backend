package com.sarthi.Sleeper.entity.scrap;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * One uniquely identified sleeper covered by a scrap verification.
 * active_key (plant|batch|sleeper) is unique while the item is active, so a sleeper can be scrapped only once;
 * it is cleared when the item is removed or its verification deleted, which puts the sleeper back in the basket.
 */
@Data
@Entity
@Table(name = "sleeper_scrap_verification_item")
public class SleeperScrapVerificationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scrap_verification_id", nullable = false)
    private Long scrapVerificationId;

    @Column(name = "plant_id", nullable = false, length = 100)
    private String plantId;

    @Column(name = "batch_no", length = 100)
    private String batchNo;

    /** Sleeper ID as shown to the IE (e.g. 210-A, 1AS) */
    @Column(name = "sleeper_no", nullable = false, length = 100)
    private String sleeperNo;

    /** Normalised batch|sleeper identity used to match rejections */
    @Column(name = "sleeper_key", nullable = false, length = 250)
    private String sleeperKey;

    /** plant|batch|sleeper while active, NULL once released */
    @Column(name = "active_key", length = 300, unique = true)
    private String activeKey;

    @Column(name = "sleeper_type", length = 100)
    private String sleeperType;

    /** DEMOULDING / FINAL / TESTING (snapshot of the rejection) */
    @Column(name = "stage", length = 30)
    private String stage;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "rejection_date")
    private LocalDate rejectionDate;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) createdDate = LocalDateTime.now();
    }
}
