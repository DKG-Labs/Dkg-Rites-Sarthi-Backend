package com.sarthi.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MR (Moment of Resistance) lot rejection rules, bound from {@code sleeper.mr.*} in application.properties.
 * <p>
 * A first test can end in "Retest": 2 other sleepers of the same lot are then declared for double testing
 * ({@link #retestTestType}). A {@link #failResult} in double testing rejects the whole lot
 * (batch returned to vendor, every sleeper rejected).
 */
@Data
@Component
@ConfigurationProperties(prefix = "sleeper.mr")
public class MrLotRejectionProperties {

    /** MR test type of a sample declared for double testing (the retest of 2 other sleepers). */
    private String retestTestType;

    /** MR test result that counts as a failure. */
    private String failResult;

    /** Reason stored on the rejected batch and shown to the IE and the vendor. */
    private String rejectionReason;

    /** The rules have no safe built-in values, so a missing property stops the application at startup. */
    @PostConstruct
    void validate() {
        if (isBlank(retestTestType) || isBlank(failResult) || isBlank(rejectionReason)) {
            throw new IllegalStateException(
                    "sleeper.mr.retest-test-type, sleeper.mr.fail-result and sleeper.mr.rejection-reason must be set");
        }
    }

    /** True when an MR sample declaration's test type is the double testing (retest) type. */
    public boolean isRetest(String mrTestType) {
        return matches(mrTestType, retestTestType);
    }

    /** True when an MR test result is the failure result. */
    public boolean isFail(String testResult) {
        return matches(testResult, failResult);
    }

    private static boolean matches(String value, String expected) {
        return value != null && expected != null && value.trim().equalsIgnoreCase(expected.trim());
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
