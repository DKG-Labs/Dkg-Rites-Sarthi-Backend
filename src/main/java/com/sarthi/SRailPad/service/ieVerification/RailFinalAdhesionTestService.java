package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalAdhesionTestRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalAdhesionTestResponseDto;

import java.util.List;

public interface RailFinalAdhesionTestService {
    RailFinalAdhesionTestResponseDto save(RailFinalAdhesionTestRequestDto requestDto);
    RailFinalAdhesionTestResponseDto getById(Long id);
    RailFinalAdhesionTestResponseDto getByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalAdhesionTestResponseDto> getByCallNo(String callNo);
    void delete(Long id);
}
