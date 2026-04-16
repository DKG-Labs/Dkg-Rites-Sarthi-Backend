package com.sarthi.Scheduked;

import com.sarthi.service.Impl.CrisPoAsyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class CrisPoScheduler {
    @Autowired
    private CrisPoAsyncService asyncService;

    @Scheduled(cron = "0 0 15 * * ?")
    public void runDailyPoSync() {
        System.out.println("Scheduler triggered");

        String date = LocalDate.now().minusDays(1).toString();

        asyncService.syncPos(date);
      //  asyncService.syncPos(String.valueOf(LocalDate.now().minusDays(1)));

      //   asyncService.syncPos("2025-12-23");

         asyncService.syncMa("2019-06-19");

       //  asyncService.syncAmendedPo("2019-06-13");

       //  asyncService.syncPoCancellations("2019-06-13");

    }
/*
    private void fetchLastYearPos(String inputDate) {

        LocalDate endDate = LocalDate.parse(inputDate);
        LocalDate startDate = endDate.minusYears(1);

        ExecutorService executor = Executors.newFixedThreadPool(5);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {

            String formattedDate = date.toString();

            executor.submit(() -> {

                try {

                    System.out.println("Processing date: " + formattedDate);

                    asyncService.syncPos(formattedDate);

                    Thread.sleep(200);

                } catch (Exception e) {

                    System.out.println("Error syncing date: " + formattedDate);
                    e.printStackTrace();
                }

            });
        }

        executor.shutdown();

        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("CRIS PO Sync Completed");
    }*/

}
