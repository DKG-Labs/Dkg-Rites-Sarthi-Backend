package com.sarthi.Sleeper.service;


import com.sarthi.Sleeper.dto.MoistureAnalysisRequestDTO;
import com.sarthi.Sleeper.dto.MoistureAnalysisResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MoistureAnalysisEntryService {

  public MoistureAnalysisResponseDTO create(MoistureAnalysisRequestDTO dto);

   public List<MoistureAnalysisResponseDTO> getAll();

   public MoistureAnalysisResponseDTO update(Long id, MoistureAnalysisRequestDTO dto);
}
