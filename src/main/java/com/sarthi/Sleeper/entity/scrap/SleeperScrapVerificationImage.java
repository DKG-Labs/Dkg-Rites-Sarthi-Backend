package com.sarthi.Sleeper.entity.scrap;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/** Before / after destruction photo of a sleeper scrap verification. */
@Data
@Entity
@Table(name = "sleeper_scrap_verification_image")
public class SleeperScrapVerificationImage {

    public static final String BEFORE = "BEFORE";
    public static final String AFTER = "AFTER";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scrap_verification_id", nullable = false)
    private Long scrapVerificationId;

    /** BEFORE or AFTER */
    @Column(name = "photo_type", nullable = false, length = 10)
    private String photoType;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "blob_name", nullable = false)
    private String blobName;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) createdDate = LocalDateTime.now();
    }
}
