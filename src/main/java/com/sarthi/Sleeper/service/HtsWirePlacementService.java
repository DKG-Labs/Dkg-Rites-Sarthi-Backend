package com.sarthi.Sleeper.service;


import com.sarthi.Sleeper.dto.HtsWirePlacementRequestDTO;
import com.sarthi.Sleeper.dto.HtsWirePlacementResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface HtsWirePlacementService {


   public HtsWirePlacementResponseDTO create(HtsWirePlacementRequestDTO dto);

   public HtsWirePlacementResponseDTO getById(Long id);

   public List<HtsWirePlacementResponseDTO> getAll();

   public HtsWirePlacementResponseDTO update(Long id, HtsWirePlacementRequestDTO dto);

    void delete(Long id);
}
