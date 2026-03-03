package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.Dowel.DowelRequestDto;
import com.sarthi.Sleeper.dto.Dowel.DowelResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DowelService {

   public DowelResponseDto create(DowelRequestDto dto);

   public DowelResponseDto update(Long id, DowelRequestDto dto);

   public DowelResponseDto getById(Long id);

   public List<DowelResponseDto> getAll();

   public void delete(Long id);
}
