package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.PlantProfile.PlantProfileRequestDto;
import com.sarthi.Sleeper.dto.PlantProfile.PlantProfileResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PlantProfileService {

   public PlantProfileResponseDto create(PlantProfileRequestDto dto);

   public PlantProfileResponseDto update(Long id, PlantProfileRequestDto dto);

   public PlantProfileResponseDto getById(Long id);

   public List<PlantProfileResponseDto> getAll();

   public void delete(Long id);

   public List<Integer> getDistinctShedsByVendorCode(String vendorCode);
}
