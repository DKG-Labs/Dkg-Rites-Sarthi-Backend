package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalVisualDimensionalInspectionRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalVisualDimensionalInspectionResponseDto;
import java.util.List;

public interface RailFinalVisualDimensionalInspectionService {
    RailFinalVisualDimensionalInspectionResponseDto save(RailFinalVisualDimensionalInspectionRequestDto dto);
    RailFinalVisualDimensionalInspectionResponseDto getById(Long id);
    RailFinalVisualDimensionalInspectionResponseDto getByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalVisualDimensionalInspectionResponseDto> getByCallNo(String callNo);
    void delete(Long id);
}
