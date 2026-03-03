package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.AdmixtureRequestDto;
import com.sarthi.Sleeper.dto.AdmixtureResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AdmixtureInventoryService {


   public AdmixtureResponseDto create(AdmixtureRequestDto dto);

   public AdmixtureResponseDto update(Long id, AdmixtureRequestDto dto);

   public AdmixtureResponseDto getById(Long id);

   public List<AdmixtureResponseDto> getAll();

   public void delete(Long id);
}
