package com.sarthi.util;

import com.sarthi.repository.NotificationBoardRepository.NotificationMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class NotificationNumberGenerator {

    @Autowired
    private NotificationMasterRepository notificationRepository;

    public String generateNotificationNumber() {

        String datePart =
                LocalDate.now()
                        .format(DateTimeFormatter.ofPattern("ddMMyy"));

        String prefix = "SN-" + datePart;

        Long count =
                notificationRepository.countTodayNotifications(
                        LocalDate.now());

        return prefix + String.format("%04d", count + 1);
    }
}
