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


    //  daily -> 9 :45 AM
   // @Scheduled(cron = "0 45 10 * * ?")
    public void createEntries() {

        ibsService.createInitialEntries();
    }

    //retry 2 days one time 2pm
 //   @Scheduled(cron = "0 0 2 */2 * ?")
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

    @Scheduled(cron = "0 04 12 * * ?")
    public void fetchBillingData() {

       ibsService.processBilling();
    }

}
