package com.sarthi.Sleeper.service;


import com.sarthi.Sleeper.dto.DemouldingInspectionRequestDTO;
import com.sarthi.Sleeper.dto.DemouldingInspectionResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DemouldingInspectionService {


   public DemouldingInspectionResponseDTO create(
            DemouldingInspectionRequestDTO dto);

   public DemouldingInspectionResponseDTO update(
            Long id,
            DemouldingInspectionRequestDTO dto);

   public DemouldingInspectionResponseDTO getById(Long id);

   public List<DemouldingInspectionResponseDTO> getAll();

   public void delete(Long id);
}
