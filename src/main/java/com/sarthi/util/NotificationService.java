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

}
