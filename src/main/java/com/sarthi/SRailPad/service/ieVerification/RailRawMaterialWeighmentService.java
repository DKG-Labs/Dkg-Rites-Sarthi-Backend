package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailRawMaterialWeighmentRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailRawMaterialWeighmentResponseDto;
import java.time.LocalDate;
import java.util.List;

public interface RailRawMaterialWeighmentService {
    RailRawMaterialWeighmentResponseDto create(RailRawMaterialWeighmentRequestDto dto);
    RailRawMaterialWeighmentResponseDto update(Long id, RailRawMaterialWeighmentRequestDto dto);
    RailRawMaterialWeighmentResponseDto getById(Long id);
    List<RailRawMaterialWeighmentResponseDto> getByShiftAndDate(String plantId, String shift, LocalDate castingDate);
    List<RailRawMaterialWeighmentResponseDto> getByPlant(String plantId, LocalDate fromDate, LocalDate toDate);
    void delete(Long id);
}
