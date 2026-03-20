package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.HtsWire.HtsWireDailyTestRequestDto;
import com.sarthi.Sleeper.dto.HtsWire.HtsWireDailyTestResponseDto;
import java.util.List;

public interface HtsWireDailyTestService {
    HtsWireDailyTestResponseDto create(HtsWireDailyTestRequestDto dto);
    HtsWireDailyTestResponseDto update(Long id, HtsWireDailyTestRequestDto dto);
    HtsWireDailyTestResponseDto getById(Long id);
    HtsWireDailyTestResponseDto getByRequestId(Long requestId);
    List<HtsWireDailyTestResponseDto> getAll();
    void delete(Long id);
}
