package com.sarthi.Sms.util;


import com.sarthi.repository.UserMasterRepository;
import com.sarthi.entity.UserMaster;
import com.sarthi.Sms.exception.SmsErrorDetails;
import com.sarthi.Sms.exception.SmsInvalidArgumentException;
import com.sarthi.constant.AppConstant;
import com.sarthi.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

@Component
public class SmsCommonUtils implements ApplicationContextAware {
    private static JwtService jwtService;
    private static UserMasterRepository userMasterRepository;

    @Autowired
    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
        jwtService = context.getBean(JwtService.class);
        userMasterRepository = context.getBean(UserMasterRepository.class);
    }

    public static Integer getUserIdFromAuthHeader(String authHeader){
        if((authHeader == null) || (!authHeader.startsWith("Bearer "))){
            throw new SmsInvalidArgumentException(
                new SmsErrorDetails(AppConstant.INVALID_TOKEN_CODE,
                                    AppConstant.INVALID_TOKEN_TYPE,
                                    AppConstant.ERROR_TYPE_ERROR,
                                    "Invalid Token")
                                );
        }
        String subject = jwtService.extractUserId(authHeader.substring(7));
        UserMaster user = null;
        try {
            Integer uid = Integer.valueOf(subject.trim());
            user = userMasterRepository.findByUserId(uid).orElse(null);
        } catch (Exception ignored) {
        }

        if (user == null) {
            user = userMasterRepository.findFirstByUserName(subject)
                    .orElseThrow(() -> new SmsInvalidArgumentException(
                            new SmsErrorDetails(AppConstant.ERROR_CODE_VALIDATION,
                                    AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "User not found")
                    ));
        }
        return user.getUserId();
    }

    public static LocalDate convertStringToDateObject(String dateString){

        try{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(dateString, formatter);
        }
        catch(Exception e){
            throw new SmsInvalidArgumentException(
                new SmsErrorDetails(AppConstant.INVALID_DATE_CODE,
                        AppConstant.INVALID_DATE_TYPE, 
                        AppConstant.ERROR_TYPE_ERROR,
                        "Invalid date format"
                    )
            );
        }

    }

    // Utility function to extract time from LocalDateTime
    public static String extractTime(LocalDateTime dateTime) {
        if(Objects.isNull(dateTime)) return null;
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return dateTime.format(timeFormatter);  // Format to get only time
    }

    public static String convertDateToString(LocalDate date) {
        if(Objects.isNull(date)) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }

    public static LocalTime convertStringToTimeObj(String timeString) {
       try {
            // Parse the time string to a LocalTime object
            return LocalTime.parse(timeString);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format. Expected HH:mm:ss.", e);
        }
    }

    public static String convertTimeToString(LocalTime localTime){
        // Define the time format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        // Format the LocalTime and return it as a string
        return localTime.format(formatter);
    }
}
