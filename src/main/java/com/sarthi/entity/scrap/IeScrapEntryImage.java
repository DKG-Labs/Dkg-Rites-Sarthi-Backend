package com.sarthi.entity.scrap;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "ie_scrap_entry_images")
@Data
public class IeScrapEntryImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "scrap_entry_id", nullable = false)
    private Integer scrapEntryId;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "blob_name", nullable = false, length = 255)
    private String blobName;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now();
}
