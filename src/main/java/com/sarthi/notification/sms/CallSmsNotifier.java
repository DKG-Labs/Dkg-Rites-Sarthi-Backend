package com.sarthi.notification.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Entry point for "call assigned / reassigned" SMS (IE, vendor, Controlling Manager).
 * Safe to call from inside any workflow transaction: nothing is sent unless that transaction commits,
 * and the sending runs async (CallSmsSender), so the workflow is never slowed down or failed by SMS.
 * Switched off unless  xpertsms.call-notifications.enabled=true.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallSmsNotifier {

    private final CallSmsSender sender;
    private final JdbcTemplate jdbc;

    @Value("${xpertsms.call-notifications.enabled:false}")
    private boolean enabled;

    /** First IE assignment of a call (ERC CALL_REGISTERED, Sleeper / Rail Pad VERIFY → Main IE). */
    public void callAssigned(String callNo, Integer ieUserId) {
        if (!enabled || callNo == null) return;
        afterCommit(() -> sender.assigned(callNo, ieUserId));
    }

    /**
     * Call moved from one IE to another. Sent only when the call had already been assigned
     * (a remap of a call still pending RIO verification sends nothing — its assignment SMS goes out on verify).
     */
    public void callReassigned(String callNo, Integer oldIeUserId, Integer newIeUserId) {
        if (!enabled || callNo == null || newIeUserId == null || newIeUserId.equals(oldIeUserId)) return;
        try {
            if (!wasAssigned(callNo.trim().toUpperCase())) return;
        } catch (Exception e) {
            log.warn("Call SMS: could not check assignment state of {}: {}", callNo, e.getMessage());
            return;
        }
        afterCommit(() -> sender.reassigned(callNo, oldIeUserId, newIeUserId));
    }

    private boolean wasAssigned(String callNo) {
        CallSmsSender.Product p = CallSmsSender.productOf(callNo);
        if (p == null) return false;
        String sql = switch (p) {
            case ERC -> "SELECT COUNT(*) FROM workflow_transition WHERE UPPER(REQUESTID) = ? AND STATUS = 'CALL_REGISTERED'";
            case SLEEPER -> "SELECT COUNT(*) FROM sleeper_workflow_transaction WHERE UPPER(request_id) = ? AND action = 'VERIFY' AND next_role = 'Main IE'";
            case RAILPAD -> "SELECT COUNT(*) FROM rail_workflow_transaction WHERE UPPER(request_id) = ? AND action = 'VERIFY' AND next_role = 'Rail Main IE'";
        };
        Integer n = jdbc.queryForObject(sql, Integer.class, callNo);
        return n != null && n > 0;
    }

    /** Runs after commit; never lets an SMS problem reach the user's request. */
    private static void afterCommit(Runnable r) {
        Runnable safe = () -> {
            try { r.run(); } catch (Exception e) { log.warn("Call SMS hand-off failed: {}", e.getMessage()); }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { safe.run(); }
            });
        } else {
            safe.run();
        }
    }
}
