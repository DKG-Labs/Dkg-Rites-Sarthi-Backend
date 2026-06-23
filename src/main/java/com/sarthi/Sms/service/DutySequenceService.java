package com.sarthi.Sms.service;



import com.sarthi.Sms.util.DutyEnum;

import java.time.LocalDate;

public interface DutySequenceService {
    public String generateDutyId(DutyEnum dutyEnum, LocalDate date);
}
