package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalSecantStiffnessTestRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalSecantStiffnessTestResponseDto;

import java.util.List;

public interface RailFinalSecantStiffnessTestService {
    RailFinalSecantStiffnessTestResponseDto save(RailFinalSecantStiffnessTestRequestDto requestDto);
    RailFinalSecantStiffnessTestResponseDto getById(Long id);
    RailFinalSecantStiffnessTestResponseDto getByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalSecantStiffnessTestResponseDto> getByCallNo(String callNo);
    void delete(Long id);
}
