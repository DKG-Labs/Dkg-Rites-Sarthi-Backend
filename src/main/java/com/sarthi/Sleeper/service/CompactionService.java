package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.CompactionDtos.CompactionRequestDto;
import com.sarthi.Sleeper.dto.CompactionDtos.CompactionResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CompactionService {

   public CompactionResponseDto create(CompactionRequestDto dto);

   public CompactionResponseDto update(Long id, CompactionRequestDto dto);

   public CompactionResponseDto getById(Long id);

   public List<CompactionResponseDto> getAll();

   public void delete(Long id);

   public List<CompactionResponseDto> getRecordsByDate(
           String plantId,
           String vendorCode,
           String shift,
           int createdBy,
           String date);

}
