package com.sarthi.util;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall;
import com.sarthi.SRailPad.entity.raipadMapping.RailPoiIeMapping;
import com.sarthi.SRailPad.repository.RailPoiIeMappingRepository;
import com.sarthi.SRailPad.repository.inspectionCall.RailInspectionCallRepository;
import com.sarthi.Sleeper.entity.FInalCall.SleeperSchedule;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCall;
import com.sarthi.Sleeper.entity.SleeperPoiIeMapping;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.SleeperInspectionCallRepository;
import com.sarthi.Sleeper.repository.SleeperPoiIeMappingRepository;
import com.sarthi.Sleeper.repository.SleeperScheduleRepository;
import com.sarthi.constant.AppConstant;
import com.sarthi.entity.*;
import com.sarthi.entity.Mail.MailNotificationMaster;
import com.sarthi.entity.Mail.NotificationHistory;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.repository.*;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final IePincodePoiMappingRepository iePincodePoiMappingRepository;
    private final PoiProcessIeMappingRepository poiProcessIeMappingRepository;
    private final ObjectMapper objectMapper;
    private final RioUserRepository rioUserRepository;
    private final UserMasterRepository userMasterRepository;
    private final InspectionCallRepository inspectionCallRepository;

    private final InspectionScheduleRepository scheduleRepository;
    private final NotificationHistoryRepository notificationHistoryRepository;
    private final MailNotificationMasterRepository mailNotificationMasterRepository;

    private final SleeperScheduleRepository sleeperScheduleRepository;
    private final SleeperPoiIeMappingRepository poiIeMappingRepository;
    private final SleeperInspectionCallRepository sleeperInspectionCallRepository;
    private final RailInspectionCallRepository railInspectionCallRepository;

    private final RailPoiIeMappingRepository railPoiIeMappingRepository;

    @Value("${spring.mail.username}")
    private String senderMail;

    //feedback notification
   /* @Override
    @Async
    public void sendEmail(
            String to,
            String subject,
            String templateName,
            Map<String, Object> variables) {

        try {

            Context context = new Context();
            context.setVariables(variables);

            String htmlContent =
                    templateEngine.process(templateName, context);

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderMail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("Mail sent successfully to {}", to);

        } catch (MessagingException e) {

            log.error(
                    "Failed to send mail to {} : {}",
                    to,
                    e.getMessage(),
                    e);
        }
    }*/
    @Override
    @Async
    public void sendEmail(
            String to,
            String subject,
            String templateName,
            Map<String, Object> variables) {

        try {

            Context context = new Context();
            context.setVariables(variables);

            String html =
                    templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderMail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);

            log.info("Mail sent successfully to {}", to);

        } catch (Exception ex) {

            log.error("Mail sending failed", ex);

            // Save only failed mail
            saveFailedNotification(
                    to,
                    subject,
                    templateName,
                    variables,
                    ex.getMessage()
            );
        }
    }


    //rio notification
    @Override
    @Async
    public void sendInspectionCallAssignedToRio(
            String productType,
            String rio,
            String requestId) {

        try {

            String vendorCode;
            String vendorName;
            String inspectionType;

            // ===================== ERC =====================
            if ("ERC".equalsIgnoreCase(productType) || "Rail pad".equalsIgnoreCase(productType)) {

                InspectionCall inspectionCall = inspectionCallRepository
                        .findByIcNumber(requestId)
                        .orElseThrow(() ->
                                new RuntimeException("Inspection Call not found : " + requestId));

                vendorCode = inspectionCall.getVendorId();
                vendorName = inspectionCall.getPoNo(); // Replace with actual vendor name if available
                inspectionType = inspectionCall.getTypeOfCall(); // Raw Material / Process / Final
            }
            else if("Rail pad".equalsIgnoreCase(productType)) {

                RailInspectionCall inspectionCall = railInspectionCallRepository
                        .findByCallNo(requestId)
                        .orElseThrow(() ->
                                new RuntimeException("Inspection Call not found : " + requestId));

                vendorCode = inspectionCall.getVendorCode();
                vendorName = inspectionCall.getVendorName(); // Replace with actual vendor name if available
                inspectionType = "Final"; // Raw Material / Process / Final
            }

            // ===================== Sleeper =====================
            else if ("SLEEPER".equalsIgnoreCase(productType)) {



                SleeperInspectionCall sleeperCall = sleeperInspectionCallRepository
                        .findByCallNo(requestId)
                        .orElseThrow(() ->
                                new RuntimeException("Sleeper Inspection Call not found : " + requestId));
                UserMaster um =  userMasterRepository.findByUserId(Math.toIntExact(sleeperCall.getCreatedBy()))
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_INVALID,
                                        AppConstant.ERROR_TYPE_CODE_INVALID,
                                        AppConstant.ERROR_TYPE_INVALID,
                                        "Invalid credentials."
                                )));
                vendorCode = String.valueOf(um.getUsername());     // change field name if different
                vendorName = um.getFullName();   // change field name if different
                inspectionType = "Final";
            }

            else {
                throw new RuntimeException("Unsupported Product Type : " + productType);
            }

            List<RioUser> rioUsers = rioUserRepository.findByRio(rio);

            if (rioUsers.isEmpty()) {
                log.info("No RIO users found for {}", rio);
                return;
            }

            for (RioUser rioUser : rioUsers) {

                UserMaster user =
                        userMasterRepository.findByEmployeeCode(rioUser.getEmployeeCode());

                if (user == null ||
                        user.getEmail() == null ||
                        user.getEmail().isBlank()) {
                    continue;
                }

                Map<String, Object> variables = new HashMap<>();

                variables.put("rioName", user.getFullName());
                variables.put("requestId", requestId);
                variables.put("vendorCode", vendorCode);
                variables.put("vendorName", vendorName);
                variables.put("rio", rio);
                variables.put("inspectionType", inspectionType);
                variables.put("productType", productType);

                sendEmail(
                        user.getEmail(),
                        "Inspection Call Assigned",
                        "rio-inspection-call",
                        variables
                );
            }

            log.info("{} Inspection Call notification sent successfully", productType);

        } catch (Exception ex) {

            log.error("Failed to send RIO notification", ex);

        }
    }



    //erc call registration notification
    @Override
    @Async
    public void sendCallRegisteredNotification(
            String requestId,
            String poiCode,
            String status) {

        try {

            InspectionCall call = inspectionCallRepository
                    .findByIcNumber(requestId)
                    .orElseThrow(() -> new RuntimeException("Inspection Call not found"));

            String inspectionType = call.getTypeOfCall();

            if ("Process".equalsIgnoreCase(inspectionType)) {

                sendProcessIeNotification(call, poiCode, status);

            } else {

                sendPrimaryIeNotification(call, poiCode, status);

            }

            sendVendorNotification(call, status);

        } catch (Exception ex) {

            log.error("Failed to send Call Registered notification", ex);

        }

    }

    private void sendPrimaryIeNotification(
            InspectionCall call,
            String poiCode,
            String status) {

        List<IePincodePoiMapping> mappings =
                iePincodePoiMappingRepository.findByPoiCode(poiCode);

        for (IePincodePoiMapping mapping : mappings) {

            UserMaster user =
                    userMasterRepository.findByEmployeeCode(mapping.getEmployeeCode());

            if (user == null || user.getEmail() == null)
                continue;

            Map<String,Object> vars = buildVariables(call, status, user.getFullName());

            sendEmail(
                    user.getEmail(),
                    "Inspection Call Registered",
                    "ie-call-registered",
                    vars
            );
        }

    }

    private void sendProcessIeNotification(
            InspectionCall call,
            String poiCode,
            String status) {

        List<PoiProcessIeMapping> mappings =
                poiProcessIeMappingRepository.findByPoiCode(poiCode);

        for (PoiProcessIeMapping mapping : mappings) {

            UserMaster user =
                    userMasterRepository.findByEmployeeCode(mapping.getEmployeeCode());

            if (user == null || user.getEmail() == null)
                continue;

            Map<String,Object> vars = buildVariables(call, status, user.getFullName());

            sendEmail(
                    user.getEmail(),
                    "Inspection Call Registered",
                    "ie-call-registered",
                    vars
            );
        }

    }


    private void sendVendorNotification(
            InspectionCall call,
            String status) {

     UserMaster um =  userMasterRepository.findByUserName(call.getVendorId())
               .orElseThrow(() -> new BusinessException(
                       new ErrorDetails(
                               AppConstant.ERROR_CODE_INVALID,
                               AppConstant.ERROR_TYPE_CODE_INVALID,
                               AppConstant.ERROR_TYPE_INVALID,
                               "Invalid credentials."
                       )));


        Map<String,Object> vars = new HashMap<>();

        vars.put("vendorName", um.getUsername());
        vars.put("callNo", call.getIcNumber());
        vars.put("status", status);

        sendEmail(
                um.getEmail(),
                "Inspection Call Registered",
                "vendor-call-registered",
                vars
        );

    }

    private Map<String,Object> buildVariables(
            InspectionCall call,
            String status,
            String ieName){

        UserMaster um =  userMasterRepository.findByUserName(call.getVendorId())
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_INVALID,
                                AppConstant.ERROR_TYPE_CODE_INVALID,
                                AppConstant.ERROR_TYPE_INVALID,
                                "Invalid credentials."
                        )));
        Map<String,Object> vars = new HashMap<>();

        vars.put("ieName", ieName);
        vars.put("callNo", call.getIcNumber());
        vars.put("vendorCode", um.getUsername());
        vars.put("poi", call.getPlaceOfInspection());
        vars.put("status", status);

        return vars;

    }


    @Override
    @Async
    public void sendInspectionScheduledNotification(
            String productType,
            String callNo,
            Integer ieUserId) {

        try {

            String vendorCode;
            LocalDate scheduleDate;
            String status;

            // =========================================================
            // ERC
            // =========================================================
            if ("ERC".equalsIgnoreCase(productType)) {

                InspectionCall inspectionCall = inspectionCallRepository
                        .findByIcNumber(callNo)
                        .orElseThrow(() ->
                                new RuntimeException("Inspection Call not found"));

                InspectionSchedule schedule = scheduleRepository
                        .findByCallNo(callNo)
                        .orElseThrow(() ->
                                new RuntimeException("Schedule not found"));

                vendorCode = inspectionCall.getVendorId();
                scheduleDate = schedule.getScheduleDate();
                status = schedule.getStatus();
            }

            // =========================================================
            // Sleeper
            // =========================================================
            else if ("SLEEPER".equalsIgnoreCase(productType)) {

                SleeperInspectionCall inspectionCall = sleeperInspectionCallRepository
                        .findByCallNo(callNo)
                        .orElseThrow(() ->
                                new RuntimeException("Sleeper Inspection Call not found"));

                SleeperSchedule schedule = sleeperScheduleRepository
                        .findByCallNo(callNo)
                        .orElseThrow(() ->
                                new RuntimeException("Sleeper Schedule not found"));

              Long vendorId = inspectionCall.getCreatedBy();
                UserMaster um =  userMasterRepository.findByUserId(Math.toIntExact(vendorId))
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_INVALID,
                                        AppConstant.ERROR_TYPE_CODE_INVALID,
                                        AppConstant.ERROR_TYPE_INVALID,
                                        "Invalid credentials."
                                )));
                vendorCode = um.getUsername();
                scheduleDate = schedule.getScheduleDate();
                status = "Scheduled";
            }

            else {
                throw new RuntimeException("Invalid Product Type : " + productType);
            }

            // =========================================================
            // IE Details
            // =========================================================
            UserMaster ie = userMasterRepository.findByUserId(ieUserId)
                    .orElseThrow(() ->
                            new RuntimeException("IE not found"));

            // =========================================================
            // Vendor Details
            // =========================================================
            UserMaster vendor = userMasterRepository
                    .findByUserName(vendorCode)
                    .orElseThrow(() ->
                            new RuntimeException("Vendor not found"));

            if (vendor.getEmail() == null || vendor.getEmail().isBlank()) {
                log.info("Vendor email not found for vendor {}", vendorCode);
                return;
            }

            // =========================================================
            // Mail Variables
            // =========================================================
            Map<String, Object> variables = new HashMap<>();

            variables.put("vendorName", vendor.getFullName());
            variables.put("vendorCode", vendorCode);
            variables.put("callNo", callNo);
            variables.put("scheduleDate", scheduleDate);
            variables.put("status", status);
            variables.put("ieName", ie.getFullName());
            variables.put("ieEmployeeCode", ie.getEmployeeCode());

            // Optional (if you want to show in email)
            variables.put("productType", productType);

            // =========================================================
            // Send Mail
            // =========================================================
            sendEmail(
                    vendor.getEmail(),
                    "Inspection Scheduled",
                    "inspection-scheduled-vendor",
                    variables
            );

            log.info("{} inspection schedule notification sent successfully for call {}",
                    productType, callNo);

        } catch (Exception ex) {

            log.error("{} inspection schedule notification failed for call {}",
                    productType, callNo, ex);
        }
    }


    private void saveFailedNotification(
            String to,
            String subject,
            String templateName,
            Map<String,Object> payload,
            String error){

        MailNotificationMaster notification =
                new MailNotificationMaster();

        notification.setRecipientEmail(to);
        notification.setSubject(subject);
        notification.setTemplateName(templateName);

        try{
            notification.setPayload(
                    objectMapper.writeValueAsString(payload));
        }catch(Exception e){
            notification.setPayload("{}");
        }

        notification.setStatus("FAILED");
        notification.setRetryCount(0);
        notification.setLastError(error);
        notification.setCreatedDate(LocalDateTime.now());

        mailNotificationMasterRepository.save(notification);

        NotificationHistory history =
                new NotificationHistory();

        history.setNotificationId(notification.getNotificationId());
        history.setAttemptNo(1);
        history.setStatus("FAILED");
        history.setErrorMessage(error);
        history.setAttemptedAt(LocalDateTime.now());

        notificationHistoryRepository.save(history);

    }


        public void retryFailedMails() {

            List<MailNotificationMaster> failedMails =
                    mailNotificationMasterRepository.findByStatus("FAILED");

            for (MailNotificationMaster mail : failedMails) {

                try {

                    Map<String, Object> variables =
                            objectMapper.readValue(
                                    mail.getPayload(),
                                    new TypeReference<Map<String, Object>>() {});

                    sendEmail(
                            mail.getRecipientEmail(),
                            mail.getSubject(),
                            mail.getTemplateName(),
                            variables
                    );

                    mail.setStatus("SENT");
                    mail.setSentDate(LocalDateTime.now());

                } catch (Exception ex) {

                    mail.setRetryCount(mail.getRetryCount() + 1);
                    mail.setLastError(ex.getMessage());

                    NotificationHistory history = new NotificationHistory();

                    history.setNotificationId(mail.getNotificationId());
                    history.setAttemptNo(mail.getRetryCount());
                    history.setStatus("FAILED");
                    history.setErrorMessage(ex.getMessage());
                    history.setAttemptedAt(LocalDateTime.now());

                    notificationHistoryRepository.save(history);
                }

                mailNotificationMasterRepository.save(mail);
            }
        }


//sleeper mail notification to vendor and ie after call registred
    @Override
    @Async
    public void sendSleeperCallRegisteredNotification(
            String callNo,
            String plantId,
            String status) {

        try {

            // Fetch Sleeper Call
            SleeperInspectionCall call = sleeperInspectionCallRepository
                    .findByCallNo(callNo)
                    .orElseThrow(() ->
                            new RuntimeException("Sleeper Call not found"));

            // Fetch Main IE
            SleeperPoiIeMapping mapping =
                    poiIeMappingRepository
                            .findByPlantIdAndIeType(
                                    plantId,
                                    "Main IE")
                            .orElseThrow(() ->
                                    new RuntimeException("Main IE not mapped"));

            UserMaster ie = userMasterRepository
                    .findByUserId(mapping.getIeUserId())
                    .orElseThrow(() ->
                            new RuntimeException("IE not found"));

            // Send Main IE Mail
            Map<String,Object> ieVars = new HashMap<>();

            ieVars.put("ieName", ie.getFullName());
            ieVars.put("callNo", call.getCallNo());
            ieVars.put("status", status);
            ieVars.put("plantId", plantId);

            sendEmail(
                    ie.getEmail(),
                    "Sleeper Inspection Call Registered",
                    "ie-call-registered",
                    ieVars
            );

            // Vendor
            UserMaster vendor = userMasterRepository
                    .findByUserName(String.valueOf(call.getCreatedBy()))
                    .orElseThrow(() ->
                            new RuntimeException("Vendor not found"));

            Map<String,Object> vendorVars = new HashMap<>();

            vendorVars.put("vendorName", vendor.getUsername());
            vendorVars.put("callNo", call.getCallNo());
            vendorVars.put("status", status);

            sendEmail(
                    vendor.getEmail(),
                    "Sleeper Inspection Call Registered",
                    "vendor-call-registered",
                    vendorVars
            );

        } catch (Exception ex) {

            log.error("Failed to send Sleeper Call Registered notification", ex);

        }
    }


    @Override
    @Async
    public void sendRailPadCallRegisteredNotification(
            String callNo,
            String plantId,
            String status) {

        try {

            // Fetch Rail Pad Call
            RailInspectionCall call = railInspectionCallRepository
                    .findByCallNo(callNo)
                    .orElseThrow(() ->
                            new RuntimeException("Rail Pad Call not found"));

            // Fetch Main IE
            RailPoiIeMapping mapping = railPoiIeMappingRepository
                    .findByPlantIdAndIeType(
                            plantId,
                            "Main IE")
                    .orElseThrow(() ->
                            new RuntimeException("Main IE not mapped"));

            // Fetch IE
            UserMaster ie = userMasterRepository
                    .findByUserId(mapping.getIeUserId())
                    .orElseThrow(() ->
                            new RuntimeException("IE not found"));

            // IE Mail Variables
            Map<String, Object> ieVars = new HashMap<>();

            ieVars.put("ieName", ie.getFullName());
            ieVars.put("callNo", call.getCallNo());
            ieVars.put("status", status);
            ieVars.put("plantId", plantId);

            sendEmail(
                    ie.getEmail(),
                    "Rail Pad Inspection Call Registered",
                    "ie-call-registered",
                    ieVars
            );

            // Fetch Vendor
            UserMaster vendor = userMasterRepository
                    .findByUserName(String.valueOf(call.getCreatedBy()))
                    .orElseThrow(() ->
                            new RuntimeException("Vendor not found"));

            // Vendor Mail Variables
            Map<String, Object> vendorVars = new HashMap<>();

            vendorVars.put("vendorName", vendor.getUsername());
            vendorVars.put("callNo", call.getCallNo());
            vendorVars.put("status", status);

            sendEmail(
                    vendor.getEmail(),
                    "Rail Pad Inspection Call Registered",
                    "vendor-call-registered",
                    vendorVars
            );

        } catch (Exception ex) {

            log.error("Failed to send Rail Pad Call Registered notification", ex);

        }
    }


}
