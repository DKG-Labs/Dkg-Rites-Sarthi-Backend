package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.MixDesignRequestDto;
import com.sarthi.Sleeper.dto.MixDesignResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MixDesignService {
    public MixDesignResponseDto create(MixDesignRequestDto dto);

    public MixDesignResponseDto update(Long id, MixDesignRequestDto dto);

    public MixDesignResponseDto getById(Long id);

    public List<MixDesignResponseDto> getAll();

    void delete(Long id);
}
