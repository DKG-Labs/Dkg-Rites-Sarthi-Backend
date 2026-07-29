package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalPeriodicTgaRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalPeriodicTgaResponseDto;

import java.util.List;

public interface RailFinalPeriodicTgaService {
    RailFinalPeriodicTgaResponseDto save(RailFinalPeriodicTgaRequestDto requestDto);
    RailFinalPeriodicTgaResponseDto getById(Long id);
    RailFinalPeriodicTgaResponseDto getByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalPeriodicTgaResponseDto> getByCallNo(String callNo);
    void delete(Long id);
}
