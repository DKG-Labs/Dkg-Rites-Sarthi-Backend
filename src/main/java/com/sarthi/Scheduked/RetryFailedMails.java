package com.sarthi.Scheduked;

import com.sarthi.util.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RetryFailedMails {


    private final NotificationService mailRetryService;

  //  @Scheduled(cron = "0 0 9 * * *")
    public void retryScheduler() {

        mailRetryService.retryFailedMails();

    }
}
