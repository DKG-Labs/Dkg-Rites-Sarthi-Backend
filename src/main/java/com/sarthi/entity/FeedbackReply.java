package com.sarthi.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "FEEDBACK_REPLIES")
@Data
public class FeedbackReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REPLY_ID")
    private Integer replyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FEEDBACK_ID", nullable = false)
    private FeedbackMaster feedback;

    @Column(name = "USER_ID", nullable = false)
    private String userId;

    @Column(name = "USER_CODE")
    private String userCode;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "ROLE_NAME")
    private String roleName;

    @Column(name = "PRODUCT_TYPE")
    private String productType;

    @Column(name = "REPLY_MESSAGE", columnDefinition = "TEXT")
    private String replyMessage;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate = LocalDateTime.now();
}
