package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalTensileStrengthRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalTensileStrengthResponseDto;

import java.util.List;

public interface RailFinalTensileStrengthService {
    RailFinalTensileStrengthResponseDto save(RailFinalTensileStrengthRequestDto requestDto);
    RailFinalTensileStrengthResponseDto getById(Long id);
    RailFinalTensileStrengthResponseDto getByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalTensileStrengthResponseDto> getByCallNo(String callNo);
    void delete(Long id);
}
