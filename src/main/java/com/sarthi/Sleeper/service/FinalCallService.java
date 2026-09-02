package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.FinalCalDtos.FinalCallInspectionHeaderRequest;
import com.sarthi.Sleeper.dto.FinalCalDtos.FinalCallInspectionHeaderResponse;
import com.sarthi.Sleeper.dto.FinalCalDtos.FinalCallRequestDto;
import com.sarthi.Sleeper.dto.FinalCalDtos.FinalCallResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FinalCallService {

    public FinalCallResponseDto create(FinalCallRequestDto dto);

    public FinalCallResponseDto update(FinalCallRequestDto dto);
    public List<FinalCallResponseDto> getByCallNo(String callNo);

    public FinalCallInspectionHeaderResponse create(FinalCallInspectionHeaderRequest dto);
    public FinalCallInspectionHeaderResponse update(FinalCallInspectionHeaderRequest dto);
    public FinalCallInspectionHeaderResponse getHeaderByCallNo(String callNo);

    public com.sarthi.Sleeper.entity.FInalCall.SleeperFinalResult saveOrUpdateSleeperFinalResult(com.sarthi.Sleeper.dto.FinalCalDtos.SleeperFinalResultRequestDto dto);
    public com.sarthi.Sleeper.entity.FInalCall.SleeperFinalResult getSleeperFinalResult(String callNumber);
}
