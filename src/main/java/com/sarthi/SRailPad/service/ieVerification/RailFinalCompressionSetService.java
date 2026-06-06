package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalCompressionSetRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalCompressionSetResponseDto;

import java.util.List;

public interface RailFinalCompressionSetService {
    RailFinalCompressionSetResponseDto save(RailFinalCompressionSetRequestDto requestDto);
    RailFinalCompressionSetResponseDto getById(Long id);
    RailFinalCompressionSetResponseDto getByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalCompressionSetResponseDto> getByCallNo(String callNo);
    void delete(Long id);
}
