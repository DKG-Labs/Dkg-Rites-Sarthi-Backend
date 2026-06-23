package com.sarthi.Sms.service.Impl;


import com.sarthi.Sms.entity.sms.sequence.DutySequenceEntity;
import com.sarthi.Sms.entity.sms.sequence.DutySequenceId;
import com.sarthi.Sms.repository.sms.sequence.DutySequenceRepository;
import com.sarthi.Sms.service.DutySequenceService;
import com.sarthi.Sms.util.DutyEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class DutySequenceImpl implements DutySequenceService {

    @Autowired
    DutySequenceRepository dutySequenceRepository;

    @Override
    public String generateDutyId(DutyEnum dutyEnum, LocalDate date){
        String datePart = date.format(DateTimeFormatter.ofPattern("ddMMyy"));
        int sequence = getNextSequenceForDay(dutyEnum, date);
        return dutyEnum.getCode() + datePart + String.format("%03d", sequence);
    }

    private int getNextSequenceForDay(DutyEnum dutyEnum, LocalDate date){
        DutySequenceEntity dutySequenceEntity = dutySequenceRepository.findByDutySequenceId(new DutySequenceId(dutyEnum.getCode(), date));

        if(dutySequenceEntity == null){
            dutySequenceEntity = new DutySequenceEntity(new DutySequenceId(dutyEnum.getCode(), date), 1);
            dutySequenceRepository.save(dutySequenceEntity);
            return 1;
        }

        dutySequenceEntity.setSequence(dutySequenceEntity.getSequence() + 1);
        dutySequenceRepository.save(dutySequenceEntity);
        return dutySequenceEntity.getSequence();
    }
}
