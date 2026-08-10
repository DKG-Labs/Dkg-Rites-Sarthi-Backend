
package com.sarthi.Scheduked;

import com.sarthi.repository.IbsCaseIntegrationRepository;
import com.sarthi.service.Impl.IbsServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
@AllArgsConstructor
public class IBSScheduler {


    private final IbsServiceImpl ibsService;
    private final IbsCaseIntegrationRepository repository;


    //  daily -> 10:10 AM and 5:00 PM
   // @Scheduled(cron = "0 08 10 * * ?")
    @Scheduled(cron = "0 0 19 * * ?")
    public void createEntries() {

        ibsService.createInitialEntries();
    }

    //retry 2 days one time 2pm

    @Scheduled(cron = "0 20 08 */2 * ?", zone = "Asia/Kolkata")
    public void processPendingRecords() {

        var pageable = PageRequest.of(0, 10);

        var list =
                repository
                        .findByCompletedFalseAndNextRetryTimeBeforeAndRetryCountLessThan(
                                LocalDateTime.now(),
                                5,
                                pageable
                        );

        for (var integration : list) {

            ibsService.processIntegration(integration);
        }
    }

    @Scheduled(cron = "0 20 07 * * ?")
    public void fetchBillingData() {

        ibsService.processBilling();
    }

}