package com.sarthi.service;

import java.time.LocalDate;
import java.util.Map;

public interface AdminIcService {
    Map<String, Object> getIcDetails(String callNo);

    Map<String, Object> updateIcDate(String callNo, LocalDate newDate);
}
