package com.sarthi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDTO {
    private Integer feedbackId;
    private String userId;
    private String userCode;
    private String userName;
    private String productType;
    private String roleName;
    private String subject;
    private String message;
    private String priority;
    private String status;
    private LocalDateTime createdDate;
    private List<FeedbackReplyDTO> replies;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeedbackReplyDTO {
        private Integer replyId;
        private String userId;
        private String userCode;
        private String userName;
        private String roleName;
        private String productType;
        private String replyMessage;
        private LocalDateTime createdDate;
    }
}
