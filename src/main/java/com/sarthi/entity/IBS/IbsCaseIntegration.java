package com.sarthi.entity.IBS;

import com.sarthi.entity.PoHeader;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;



@Entity
@Table(name = "ibs_case_integration")
@Data
public class IbsCaseIntegration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String poKey;

    private String poNo;

    private String rlyCd;

    private LocalDateTime poDate;

    @Column(name = "case_no", columnDefinition = "TEXT")
    private String caseNo;

    // NEW, PENDING, AVAILABLE, FAILED, MAX_RETRY
    private String status;

    private Integer retryCount = 0;

    private LocalDateTime lastAttemptTime;

    private LocalDateTime nextRetryTime;

    @Column(columnDefinition = "LONGTEXT")
    private String requestJson;

    @Column(columnDefinition = "LONGTEXT")
    private String responseJson;

    @Column(length = 2000)
    private String errorMessage;

    private Boolean completed = false;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_header_id")
    private PoHeader poHeader;

    @PrePersist
    public void prePersist() {

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (status == null) {
            status = "NEW";
        }

        if (retryCount == null) {
            retryCount = 0;
        }

        if (completed == null) {
            completed = false;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

