package com.sarthi.superadmin.dpchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Every minute: restores the previous DP of every temporary Super Admin DP change whose 2 hours are over.
 * Overdue changes (e.g. after a server restart) are picked up on the next run.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DpChangeRevertJob {

    private final DpChangeService service;

    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    public void revertDue() {
        try {
            for (Long id : service.dueIds()) {
                try {
                    service.revert(id, "REVERTED", null, "System (auto, after " + DpChangeService.REVERT_HOURS + " h)");
                } catch (Exception e) {
                    log.error("Auto-revert of DP change {} failed: {}", id, e.getMessage());
                }
            }
        } catch (Exception e) {
            // e.g. table not created yet: try again next minute
            log.debug("DP change revert job skipped: {}", e.getMessage());
        }
    }
}
