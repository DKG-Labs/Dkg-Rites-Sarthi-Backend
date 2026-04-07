package com.sarthi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardKPIsDto {
    private String rio;
    private PendingVerificationKPIs pendingVerification;
    private VerifiedOpenKPIs verifiedOpen;
    private DisposedKPIs disposed;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingVerificationKPIs {
        private long total;
        private long fresh;
        private long resubmissions;
        private long returned;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifiedOpenKPIs {
        private long total;
        private long verifiedRegistered;
        private long ieAssignmentPending;
        private long assignedToIE;
        private long scheduled;
        private long underInspection;
        private long underLabTesting;
        private long icPending;
        private long billingPending;
        private long paymentPending;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DisposedKPIs {
        private long total;
    }
}
