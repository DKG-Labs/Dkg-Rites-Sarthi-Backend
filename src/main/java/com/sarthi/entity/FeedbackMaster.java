package com.sarthi.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "FEEDBACK_MASTER")
@Data
public class FeedbackMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FEEDBACK_ID")
    private Integer feedbackId;

    @Column(name = "USER_ID", nullable = false)
    private String userId;

    @Column(name = "USER_CODE")
    private String userCode;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "PRODUCT_TYPE")
    private String productType;

    @Column(name = "ROLE_NAME")
    private String roleName;

    @Column(name = "SUBJECT", nullable = false)
    private String subject;

    @Column(name = "MESSAGE", columnDefinition = "TEXT")
    private String message;

    @Column(name = "PRIORITY")
    private String priority = "Medium";

    @Column(name = "STATUS")
    private String status = "Pending";

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate = LocalDateTime.now();

    @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedbackReply> replies = new ArrayList<>();

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
