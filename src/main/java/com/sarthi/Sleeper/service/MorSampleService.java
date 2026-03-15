package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.MorSampleRequestDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.MorSampleResponseDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.MorTestResultDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MorSampleService {

    MorSampleResponseDto create(MorSampleRequestDto dto);

    MorSampleResponseDto update(Long id, MorSampleRequestDto dto);

    MorSampleResponseDto getById(Long id);

    List<MorSampleResponseDto> getAll(Long userId);
    
    List<MorSampleResponseDto> getHistorical(Long userId);

    List<MorSampleResponseDto> getPendingMorDeclarations(Long userId);

    MorSampleResponseDto saveTestResults(Long declarationId, List<MorTestResultDto> results);

    void delete(Long id);
}
