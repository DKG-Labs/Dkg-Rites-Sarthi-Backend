package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalLoadTestRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalLoadTestResponseDto;

import java.util.List;

public interface RailFinalLoadTestService {
    RailFinalLoadTestResponseDto save(RailFinalLoadTestRequestDto requestDto);
    RailFinalLoadTestResponseDto getById(Long id);
    RailFinalLoadTestResponseDto getByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalLoadTestResponseDto> getByCallNo(String callNo);
    void delete(Long id);
}
