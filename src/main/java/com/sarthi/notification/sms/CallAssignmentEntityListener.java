package com.sarthi.notification.sms;

import com.sarthi.SRailPad.entity.RailWorkflowTransaction;
import com.sarthi.Sleeper.entity.SleeperWorkflowTransaction;
import com.sarthi.entity.WorkflowTransition;
import jakarta.persistence.PostPersist;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Catches the first IE assignment of a call wherever the workflow row is inserted
 * (there are several code paths that create these rows):
 *   ERC      : new workflow_transition row with STATUS = 'CALL_REGISTERED'
 *   Sleeper  : new sleeper_workflow_transaction row, action VERIFY, next role 'Main IE'
 *   Rail Pad : new rail_workflow_transaction row, action VERIFY, next role 'Rail Main IE'
 * Only hands the call to CallSmsNotifier (which waits for commit); never throws.
 */
@Slf4j
@Component
public class CallAssignmentEntityListener implements ApplicationContextAware {

    private static ApplicationContext ctx;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext;
    }

    @PostPersist
    public void onInsert(Object entity) {
        try {
            if (entity instanceof WorkflowTransition w) {
                if ("CALL_REGISTERED".equalsIgnoreCase(w.getStatus())) {
                    notifier().callAssigned(w.getRequestId(), w.getAssignedToUser());
                }
            } else if (entity instanceof SleeperWorkflowTransaction s) {
                if ("VERIFY".equalsIgnoreCase(s.getAction()) && "Main IE".equalsIgnoreCase(s.getNextRole())) {
                    notifier().callAssigned(s.getRequestId(), toInt(s.getAssignedToUser()));
                }
            } else if (entity instanceof RailWorkflowTransaction r) {
                if ("VERIFY".equalsIgnoreCase(r.getAction()) && "Rail Main IE".equalsIgnoreCase(r.getNextRole())) {
                    notifier().callAssigned(r.getRequestId(), toInt(r.getAssignedToUser()));
                }
            }
        } catch (Exception e) {
            log.warn("Call SMS listener skipped: {}", e.getMessage());
        }
    }

    private static CallSmsNotifier notifier() {
        return ctx.getBean(CallSmsNotifier.class);
    }

    private static Integer toInt(Long v) {
        return v == null ? null : v.intValue();
    }
}
