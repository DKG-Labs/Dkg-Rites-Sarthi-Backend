package com.sarthi.Sms.util;

import com.sarthi.Sms.constant.RailMillAppConstant;
import com.sarthi.Sms.exception.SmsErrorDetails;
import com.sarthi.Sms.exception.SmsInvalidArgumentException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * Rail-mill (Rites) helpers, ported from the Rites backend's CommonUtils.
 * The logged-in user comes from SARTHI's JWT (via {@link SmsCommonUtils}); the rail-mill code keeps
 * working with the user id as a String, as it did in the Rites backend.
 */
public final class RailMillCommonUtils {

    private RailMillCommonUtils() { }

    public static String getUserIdFromAuthHeader(String authHeader) {
        return String.valueOf(SmsCommonUtils.getUserIdFromAuthHeader(authHeader));
    }

    public static LocalDate convertStringToDateObject(String dateString) {
        try {
            return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            throw new SmsInvalidArgumentException(new SmsErrorDetails(RailMillAppConstant.INVALID_DATE_CODE,
                    RailMillAppConstant.INVALID_DATE_TYPE, RailMillAppConstant.ERROR_TYPE_ERROR, "Invalid date format"));
        }
    }

    public static String extractTime(LocalDateTime dateTime) {
        if (Objects.isNull(dateTime)) return null;
        return dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public static String convertDateToString(LocalDate date) {
        if (Objects.isNull(date)) return null;
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public static LocalTime convertStringToTimeObj(String timeString) {
        try {
            return LocalTime.parse(timeString);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format. Expected HH:mm:ss.", e);
        }
    }

    public static String convertTimeToString(LocalTime localTime) {
        return localTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}
