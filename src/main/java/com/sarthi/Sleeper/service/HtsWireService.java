package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.HtsWire.HtsWireRequestDto;
import com.sarthi.Sleeper.dto.HtsWire.HtsWireResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface HtsWireService {

    HtsWireResponseDto create(HtsWireRequestDto dto);

    HtsWireResponseDto update(Long id, HtsWireRequestDto dto);

    HtsWireResponseDto getById(Long id);

    List<HtsWireResponseDto> getAll();

    void delete(Long id);
}
