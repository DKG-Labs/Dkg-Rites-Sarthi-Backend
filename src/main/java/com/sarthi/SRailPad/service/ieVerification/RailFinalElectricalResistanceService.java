package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalElectricalResistanceRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalElectricalResistanceResponseDto;

import java.util.List;

public interface RailFinalElectricalResistanceService {
    RailFinalElectricalResistanceResponseDto save(RailFinalElectricalResistanceRequestDto dto);
    RailFinalElectricalResistanceResponseDto getById(Long id);
    RailFinalElectricalResistanceResponseDto getByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalElectricalResistanceResponseDto> getByCallNo(String callNo);
    void delete(Long id);
}
