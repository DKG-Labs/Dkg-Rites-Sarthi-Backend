package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalInspectionLotResultsRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalInspectionLotResultsResponseDto;
import java.time.LocalDate;
import java.util.List;

public interface RailFinalInspectionLotResultsService {
    RailFinalInspectionLotResultsResponseDto save(RailFinalInspectionLotResultsRequestDto dto);
    RailFinalInspectionLotResultsResponseDto getById(Long id);
    List<RailFinalInspectionLotResultsResponseDto> getByCallNo(String callNo);
    List<RailFinalInspectionLotResultsResponseDto> getByShiftAndDate(String plantId, String shift, LocalDate dateOfInspection);
    void delete(Long id);
}
