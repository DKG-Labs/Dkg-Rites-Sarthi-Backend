package com.sarthi.util;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface NotificationService {

    void sendEmail(
            String to,
            String subject,
            String templateName,
            Map<String, Object> variables);

    void sendInspectionCallAssignedToRio( String productType,String rio, String requestId);

    void sendCallRegisteredNotification(
            String requestId,
            String poiCode,
            String status
    );

    public void sendInspectionScheduledNotification(
            String productType,
            String callNo,
            Integer ieUserId);

    void retryFailedMails();

    void sendSleeperCallRegisteredNotification(String requestId, String plantId, String callRegistered);

    public void sendRailPadCallRegisteredNotification(
            String callNo,
            String plantId,
            String status);
}
