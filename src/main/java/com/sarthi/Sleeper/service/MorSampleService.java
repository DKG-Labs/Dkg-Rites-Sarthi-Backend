package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.MorSampleRequestDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.MorSampleResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MorSampleService {

    MorSampleResponseDto create(MorSampleRequestDto dto);

    MorSampleResponseDto update(Long id, MorSampleRequestDto dto);

    MorSampleResponseDto getById(Long id);

    List<MorSampleResponseDto> getAll();

    void delete(Long id);
}
