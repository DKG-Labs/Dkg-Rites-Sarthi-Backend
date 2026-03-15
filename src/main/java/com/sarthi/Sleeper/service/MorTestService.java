package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.MorTestRequestDto;
import com.sarthi.Sleeper.dto.MorTestResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MorTestService {

    MorTestResponseDto create(MorTestRequestDto dto);

    MorTestResponseDto update(Long id, MorTestRequestDto dto);

    MorTestResponseDto getById(Long id);

    List<MorTestResponseDto> getAll();

    void delete(Long id);
}
