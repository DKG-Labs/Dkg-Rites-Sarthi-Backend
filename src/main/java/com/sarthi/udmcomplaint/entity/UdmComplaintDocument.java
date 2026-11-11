package com.sarthi.udmcomplaint.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/** A PDF uploaded for a complaint stage (file lives in the UDM complaint Azure container). */
@Entity
@Table(name = "udm_complaint_document")
@Data
public class UdmComplaintDocument {

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

    @Column(name = "doc_type", nullable = false, length = 40)
    private String docType;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "blob_name", nullable = false, length = 500)
    private String blobName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    @Column(name = "uploaded_by")
    private Integer uploadedBy;

    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        if (active == null) active = Boolean.TRUE;
    }
}
