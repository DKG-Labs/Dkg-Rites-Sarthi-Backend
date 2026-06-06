package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalNcrBreakingLoadTestRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalNcrBreakingLoadTestResponseDto;

import java.util.List;

public interface RailFinalNcrBreakingLoadTestService {
    RailFinalNcrBreakingLoadTestResponseDto save(RailFinalNcrBreakingLoadTestRequestDto requestDto);
    RailFinalNcrBreakingLoadTestResponseDto getById(Long id);
    RailFinalNcrBreakingLoadTestResponseDto getByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalNcrBreakingLoadTestResponseDto> getByCallNo(String callNo);
    void delete(Long id);
}
