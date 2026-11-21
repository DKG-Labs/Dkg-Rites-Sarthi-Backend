package com.sarthi.util;

/**
 * IC numbers are "R/CALL/IE" (RIO letter / call no / IE short name), e.g. N/RPP-101926001/CHETAN.
 * Used to swap the IE part when a call is reassigned before its IC is issued.
 */
public final class IcNumberSuffix {

    private IcNumberSuffix() { }

    /** Same IC number with the last part replaced by the given IE short name; null if the number has no IE part. */
    public static String withIe(String icNumber, String ieShortName) {
        if (icNumber == null || ieShortName == null || ieShortName.isBlank()) return null;
        String no = icNumber.trim();
        int last = no.lastIndexOf('/');
        int first = no.indexOf('/');
        if (first < 0 || last == first) return null;   // needs at least R/CALL/IE
        return no.substring(0, last + 1) + ieShortName.trim().toUpperCase();
    }
}
