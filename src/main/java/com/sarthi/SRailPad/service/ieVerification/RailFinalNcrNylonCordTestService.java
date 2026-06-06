package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalNcrNylonCordTestRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalNcrNylonCordTestResponseDto;

import java.util.List;

public interface RailFinalNcrNylonCordTestService {
    RailFinalNcrNylonCordTestResponseDto save(RailFinalNcrNylonCordTestRequestDto requestDto);
    RailFinalNcrNylonCordTestResponseDto getById(Long id);
    RailFinalNcrNylonCordTestResponseDto getByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalNcrNylonCordTestResponseDto> getByCallNo(String callNo);
    void delete(Long id);
}
