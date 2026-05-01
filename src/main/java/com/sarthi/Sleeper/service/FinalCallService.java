package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.FinalCalDtos.FinalCallRequestDto;
import com.sarthi.Sleeper.dto.FinalCalDtos.FinalCallResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FinalCallService {

    public FinalCallResponseDto create(FinalCallRequestDto dto);

    public FinalCallResponseDto update(FinalCallRequestDto dto);
    public List<FinalCallResponseDto> getByCallNo(String callNo);
}
