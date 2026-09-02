package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.FinalCalDtos.*;
import org.springframework.stereotype.Service;

@Service
public interface FinalCallInspectionService {

    public InspectionCallSection1Response getDetails(String callNo);

    public InspectionCallSection2DetailsResponse getSectionB(String callNo);

    public SectionARequest create(SectionARequest req);

    public SectionBRequest create(SectionBRequest req);

    public SleeperScheduleRequest create(SleeperScheduleRequest req);

    public SleeperScheduleRequest update(SleeperScheduleRequest req);
    
    public SleeperScheduleRequest getSchedule(String callNo);
}
