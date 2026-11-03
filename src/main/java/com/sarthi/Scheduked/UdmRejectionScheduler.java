package com.sarthi.Scheduked;

import com.sarthi.entity.UdmSyncLog;
import com.sarthi.service.Impl.UdmRejectionSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class UdmRejectionScheduler {

    @Autowired
    private UdmRejectionSyncService syncService;

    /**
     * Runs daily at 02:00 AM — fetches yesterday's rejections from CRIS and stores in DB.
     * Every run (success, no data or failure with its reason) is recorded in udm_sync_log.
     * Cron: second minute hour day month weekday
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncYesterdayRejections() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        System.out.println("[UdmScheduler] Daily sync triggered for: " + yesterday);
        try {
            UdmSyncLog log = syncService.runSync(yesterday, UdmSyncLog.TRIGGER_SCHEDULER, null);
            System.out.println("[UdmScheduler] Sync finished with status " + log.getStatus()
                    + " (received " + log.getRecordsReceived() + ", new " + log.getRecordsNew()
                    + ", updated " + log.getRecordsUpdated() + ")"
                    + (log.getErrorMessage() != null ? " - " + log.getErrorMessage() : ""));
        } catch (Exception e) {
            System.err.println("[UdmScheduler] Sync failed for " + yesterday + ": " + e.getMessage());
        }
    }
}
