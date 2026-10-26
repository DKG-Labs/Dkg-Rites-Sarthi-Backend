package com.sarthi.Scheduked;

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
     * Cron: second minute hour day month weekday
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncYesterdayRejections() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        System.out.println("[UdmScheduler] Daily sync triggered for: " + yesterday);
        try {
            int count = syncService.syncForDate(yesterday);
            System.out.println("[UdmScheduler] Sync complete. Records saved: " + count);
        } catch (Exception e) {
            System.err.println("[UdmScheduler] Sync failed for " + yesterday + ": " + e.getMessage());
        }
    }
}
