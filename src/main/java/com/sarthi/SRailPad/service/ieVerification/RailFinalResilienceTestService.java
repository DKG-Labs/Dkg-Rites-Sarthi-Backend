package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalResilienceTestRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalResilienceTestResponseDto;

import java.util.List;

public interface RailFinalResilienceTestService {
    RailFinalResilienceTestResponseDto save(RailFinalResilienceTestRequestDto requestDto);
    RailFinalResilienceTestResponseDto getById(Long id);
    RailFinalResilienceTestResponseDto getByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalResilienceTestResponseDto> getByCallNo(String callNo);
    void delete(Long id);
}
