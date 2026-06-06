package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalAshContentRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalAshContentResponseDto;

import java.util.List;

public interface RailFinalAshContentService {
    RailFinalAshContentResponseDto save(RailFinalAshContentRequestDto requestDto);
    RailFinalAshContentResponseDto getById(Long id);
    RailFinalAshContentResponseDto getByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalAshContentResponseDto> getByCallNo(String callNo);
    void delete(Long id);
}
