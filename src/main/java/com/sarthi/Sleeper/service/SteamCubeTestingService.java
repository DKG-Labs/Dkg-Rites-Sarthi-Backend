package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.SteamCubeTestingDtos.SteamCubeTestingRequestDto;
import com.sarthi.Sleeper.dto.SteamCubeTestingDtos.SteamCubeTestingResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SteamCubeTestingService {

    SteamCubeTestingResponseDto create(SteamCubeTestingRequestDto dto);

    SteamCubeTestingResponseDto update(Long id, SteamCubeTestingRequestDto dto);

    SteamCubeTestingResponseDto getById(Long id);

    List<SteamCubeTestingResponseDto> getAll();

    void delete(Long id);

    public List<SteamCubeTestingResponseDto> getByDate(
            String plantId,
            String vendorCode,
            String shift,
            int createdBy,
            String date);
}
