package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalPeriodicAbrasionRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalPeriodicAbrasionResponseDto;

import java.util.List;

public interface RailFinalPeriodicAbrasionService {
    RailFinalPeriodicAbrasionResponseDto save(RailFinalPeriodicAbrasionRequestDto requestDto);
    RailFinalPeriodicAbrasionResponseDto getById(Long id);
    RailFinalPeriodicAbrasionResponseDto getByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalPeriodicAbrasionResponseDto> getByCallNo(String callNo);
    void delete(Long id);
}
