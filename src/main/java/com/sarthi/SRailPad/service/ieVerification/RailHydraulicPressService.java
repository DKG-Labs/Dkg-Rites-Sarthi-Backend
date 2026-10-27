package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailHydraulicPressRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailHydraulicPressResponseDto;
import java.time.LocalDate;
import java.util.List;

public interface RailHydraulicPressService {
    RailHydraulicPressResponseDto create(RailHydraulicPressRequestDto dto);
    RailHydraulicPressResponseDto update(Long id, RailHydraulicPressRequestDto dto);
    RailHydraulicPressResponseDto getById(Long id);
    List<RailHydraulicPressResponseDto> getByShiftAndDate(String plantId, String shift, LocalDate castingDate);
    List<RailHydraulicPressResponseDto> getByPlant(String plantId, LocalDate fromDate, LocalDate toDate);
    void delete(Long id);
}
