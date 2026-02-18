package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.SteamCubeDtos.SteamCubeSampleDeclarationRequestDto;
import com.sarthi.Sleeper.dto.SteamCubeDtos.SteamCubeSampleDeclarationResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SteamCubeService {

  public SteamCubeSampleDeclarationResponseDto create(
            SteamCubeSampleDeclarationRequestDto dto);

    public SteamCubeSampleDeclarationResponseDto update(
            Long id,
            SteamCubeSampleDeclarationRequestDto dto);

   public SteamCubeSampleDeclarationResponseDto getById(Long id);

   public List<SteamCubeSampleDeclarationResponseDto> getAll();

    void delete(Long id);
}
