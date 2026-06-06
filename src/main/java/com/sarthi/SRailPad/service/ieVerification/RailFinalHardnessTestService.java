package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalHardnessTestRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalHardnessTestResponseDto;

import java.util.List;

public interface RailFinalHardnessTestService {
    RailFinalHardnessTestResponseDto save(RailFinalHardnessTestRequestDto requestDto);
    RailFinalHardnessTestResponseDto getById(Long id);
    RailFinalHardnessTestResponseDto getByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalHardnessTestResponseDto> getByCallNo(String callNo);
    void delete(Long id);
}
