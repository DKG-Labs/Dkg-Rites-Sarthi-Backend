package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalElongationRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalElongationResponseDto;

import java.util.List;

public interface RailFinalElongationService {
    RailFinalElongationResponseDto save(RailFinalElongationRequestDto requestDto);
    RailFinalElongationResponseDto getById(Long id);
    RailFinalElongationResponseDto getByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalElongationResponseDto> getByCallNo(String callNo);
    void delete(Long id);
}
