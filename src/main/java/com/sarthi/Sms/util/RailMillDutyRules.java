package com.sarthi.Sms.util;

import com.sarthi.Sms.constant.RailMillAppConstant;
import com.sarthi.Sms.exception.SmsErrorDetails;
import com.sarthi.Sms.exception.SmsInvalidArgumentException;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Start-duty rules for the Rolling and NDT duties (same as the start-duty forms):
 * date today or yesterday (IST); yesterday allows only shift C; organisation BSP / JSPL; JSPL has only mill URM.
 */
public final class RailMillDutyRules {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private RailMillDutyRules() {
    }

    public static void validate(LocalDate date, String shift, String organisation, String mill) {
        LocalDate today = LocalDate.now(IST);
        if (date == null || !(date.equals(today) || date.equals(today.minusDays(1)))) {
            throw invalid("Duty date must be today or yesterday");
        }
        if (date.equals(today.minusDays(1)) && !"C".equalsIgnoreCase(trim(shift))) {
            throw invalid("Only shift C can be started for yesterday's date");
        }
        String org = trim(organisation);
        if (!"BSP".equalsIgnoreCase(org) && !"JSPL".equalsIgnoreCase(org)) {
            throw invalid("Organisation must be BSP or JSPL");
        }
        if ("JSPL".equalsIgnoreCase(org) && !"URM".equalsIgnoreCase(trim(mill))) {
            throw invalid("JSPL has only the URM mill");
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static SmsInvalidArgumentException invalid(String message) {
        return new SmsInvalidArgumentException(new SmsErrorDetails(RailMillAppConstant.ERROR_CODE_VALIDATION,
                RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION, RailMillAppConstant.ERROR_TYPE_VALIDATION, message));
    }
}
