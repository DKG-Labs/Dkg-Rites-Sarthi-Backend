package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalOzoneTestRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalOzoneTestResponseDto;

import java.util.List;

public interface RailFinalOzoneTestService {
    RailFinalOzoneTestResponseDto save(RailFinalOzoneTestRequestDto requestDto);
    RailFinalOzoneTestResponseDto getById(Long id);
    RailFinalOzoneTestResponseDto getByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalOzoneTestResponseDto> getByCallNo(String callNo);
    void delete(Long id);
}
