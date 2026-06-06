package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalWeightTestRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalWeightTestResponseDto;
import java.util.List;

public interface RailFinalWeightTestService {
    RailFinalWeightTestResponseDto save(RailFinalWeightTestRequestDto dto);
    RailFinalWeightTestResponseDto getById(Long id);
    RailFinalWeightTestResponseDto getByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalWeightTestResponseDto> getByCallNo(String callNo);
    void delete(Long id);
}
