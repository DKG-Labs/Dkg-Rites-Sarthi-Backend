package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalPeriodicDurabilityRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalPeriodicDurabilityResponseDto;

import java.util.List;

public interface RailFinalPeriodicDurabilityService {
    RailFinalPeriodicDurabilityResponseDto save(RailFinalPeriodicDurabilityRequestDto requestDto);
    RailFinalPeriodicDurabilityResponseDto getById(Long id);
    RailFinalPeriodicDurabilityResponseDto getByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalPeriodicDurabilityResponseDto> getByCallNo(String callNo);
    void delete(Long id);
}
