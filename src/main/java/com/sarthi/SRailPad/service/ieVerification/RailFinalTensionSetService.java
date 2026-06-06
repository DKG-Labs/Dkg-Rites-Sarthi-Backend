package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalTensionSetRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalTensionSetResponseDto;

import java.util.List;

public interface RailFinalTensionSetService {
    RailFinalTensionSetResponseDto save(RailFinalTensionSetRequestDto requestDto);
    RailFinalTensionSetResponseDto getById(Long id);
    RailFinalTensionSetResponseDto getByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalTensionSetResponseDto> getByCallNo(String callNo);
    void delete(Long id);
}
