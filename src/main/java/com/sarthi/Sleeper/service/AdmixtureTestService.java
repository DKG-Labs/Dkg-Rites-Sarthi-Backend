package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.AdmixtureTestRequestDto;
import com.sarthi.Sleeper.dto.AdmixtureTestResponseDto;
import java.util.List;

public interface AdmixtureTestService {
    AdmixtureTestResponseDto create(AdmixtureTestRequestDto dto);
    AdmixtureTestResponseDto update(Long id, AdmixtureTestRequestDto dto);
    AdmixtureTestResponseDto getById(Long id);
    List<AdmixtureTestResponseDto> getAll();
    void delete(Long id);
}
