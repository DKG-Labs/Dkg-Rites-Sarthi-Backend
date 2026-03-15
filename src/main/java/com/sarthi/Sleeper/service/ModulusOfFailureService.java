package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.ModulusOfFailureRequestDto;
import com.sarthi.Sleeper.dto.ModulusOfFailureResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ModulusOfFailureService {
    ModulusOfFailureResponseDto create(ModulusOfFailureRequestDto dto);

    ModulusOfFailureResponseDto update(Long id, ModulusOfFailureRequestDto dto);

    ModulusOfFailureResponseDto getById(Long id);

    List<ModulusOfFailureResponseDto> getAll();

    void delete(Long id);
}
