package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.SleeperInspectionCallDetailDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.SleeperInspectionCallSubmitDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.SleeperInspectionCallListDto;
import java.util.List;

public interface SleeperInspectionCallService {
    String submitInspectionCall(SleeperInspectionCallSubmitDto submitDto);
    List<SleeperInspectionCallListDto> getVendorInspectionCalls(Long userId);
    String withdrawInspectionCall(String callNo);
    SleeperInspectionCallDetailDto getInspectionCallDetails(String callNo);
    String modifyInspectionCall(SleeperInspectionCallSubmitDto dto);
}

