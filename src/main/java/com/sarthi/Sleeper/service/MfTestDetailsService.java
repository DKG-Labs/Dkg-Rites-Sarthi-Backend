package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.MfTestDetailsRequestDto;
import com.sarthi.Sleeper.dto.MfTestDetailsResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MfTestDetailsService {

    MfTestDetailsResponseDto create(MfTestDetailsRequestDto dto);

    MfTestDetailsResponseDto update(Long id, MfTestDetailsRequestDto dto);

    MfTestDetailsResponseDto getById(Long id);

    List<MfTestDetailsResponseDto> getAll();

    void delete(Long id);
}
