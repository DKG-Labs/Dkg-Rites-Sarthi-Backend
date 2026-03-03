package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.Aggregates.AggregatesRequestDto;
import com.sarthi.Sleeper.dto.Aggregates.AggregatesResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface AggregatesService {

  public AggregatesResponseDto create(AggregatesRequestDto dto);

  public AggregatesResponseDto update(Long id, AggregatesRequestDto dto);

   public AggregatesResponseDto getById(Long id);

  public List<AggregatesResponseDto> getAll();

  public void delete(Long id);
}
