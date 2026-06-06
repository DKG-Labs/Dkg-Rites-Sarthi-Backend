package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalModulusRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalModulusResponseDto;

import java.util.List;

public interface RailFinalModulusService {
    RailFinalModulusResponseDto save(RailFinalModulusRequestDto requestDto);
    RailFinalModulusResponseDto getById(Long id);
    RailFinalModulusResponseDto getByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalModulusResponseDto> getByCallNo(String callNo);
    void delete(Long id);
}
