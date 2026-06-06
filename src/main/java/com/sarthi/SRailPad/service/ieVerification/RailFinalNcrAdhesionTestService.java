package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalNcrAdhesionTestRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalNcrAdhesionTestResponseDto;

import java.util.List;

public interface RailFinalNcrAdhesionTestService {
    RailFinalNcrAdhesionTestResponseDto save(RailFinalNcrAdhesionTestRequestDto requestDto);
    RailFinalNcrAdhesionTestResponseDto getById(Long id);
    RailFinalNcrAdhesionTestResponseDto getByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalNcrAdhesionTestResponseDto> getByCallNo(String callNo);
    void delete(Long id);
}
