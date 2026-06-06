package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalSpecificGravityRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalSpecificGravityResponseDto;

import java.util.List;

public interface RailFinalSpecificGravityService {
    RailFinalSpecificGravityResponseDto save(RailFinalSpecificGravityRequestDto dto);
    RailFinalSpecificGravityResponseDto getById(Long id);
    RailFinalSpecificGravityResponseDto getByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalSpecificGravityResponseDto> getByCallNo(String callNo);
    void delete(Long id);
}
