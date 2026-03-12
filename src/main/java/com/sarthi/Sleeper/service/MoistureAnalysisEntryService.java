package com.sarthi.Sleeper.service;


import com.sarthi.Sleeper.dto.MoistureAnalysisRequestDTO;
import com.sarthi.Sleeper.dto.MoistureAnalysisResponseDTO;
import com.sarthi.Sleeper.dto.PlantProfile.PlantProfileResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MoistureAnalysisEntryService {

  public MoistureAnalysisResponseDTO create(MoistureAnalysisRequestDTO dto);

   public List<MoistureAnalysisResponseDTO> getAll();

    public MoistureAnalysisResponseDTO getById(Long id);

   public MoistureAnalysisResponseDTO update(Long id, MoistureAnalysisRequestDTO dto);
}
