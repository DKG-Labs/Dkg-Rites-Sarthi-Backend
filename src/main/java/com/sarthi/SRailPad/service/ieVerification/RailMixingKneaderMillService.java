package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailMixingKneaderMillRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailMixingKneaderMillResponseDto;
import java.time.LocalDate;
import java.util.List;

public interface RailMixingKneaderMillService {
    RailMixingKneaderMillResponseDto create(RailMixingKneaderMillRequestDto dto);
    RailMixingKneaderMillResponseDto update(Long id, RailMixingKneaderMillRequestDto dto);
    RailMixingKneaderMillResponseDto getById(Long id);
    List<RailMixingKneaderMillResponseDto> getByShiftAndDate(String plantId, String shift, LocalDate castingDate);
    void delete(Long id);
}
