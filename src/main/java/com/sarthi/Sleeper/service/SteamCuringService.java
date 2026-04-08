package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.SteamCuring.SteamCuringRequestDto;
import com.sarthi.Sleeper.dto.SteamCuring.SteamCuringResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SteamCuringService {


   public SteamCuringResponseDto create(SteamCuringRequestDto dto);

   public SteamCuringResponseDto update(Long id, SteamCuringRequestDto dto);

   public SteamCuringResponseDto getById(Long id);

   public List<SteamCuringResponseDto> getAll();

   public void delete(Long id);

   public List<SteamCuringResponseDto> getRecordsByDate(
           String plantId,
           String vendorCode,
           String shift,
           int createdBy,
           String date);
}
