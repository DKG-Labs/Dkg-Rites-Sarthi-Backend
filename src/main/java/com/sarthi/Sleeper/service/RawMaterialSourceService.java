package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.RawMaterialSourceRequestDto;
import com.sarthi.Sleeper.dto.RawMaterialSourceResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RawMaterialSourceService {

    public RawMaterialSourceResponseDto create(RawMaterialSourceRequestDto dto);

    public RawMaterialSourceResponseDto update(Long id, RawMaterialSourceRequestDto dto);

    public RawMaterialSourceResponseDto getById(Long id);

    public List<RawMaterialSourceResponseDto> getAll();

    public void delete(Long id);
}
