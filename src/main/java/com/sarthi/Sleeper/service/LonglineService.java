package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.LonglineRequestDTO;
import com.sarthi.Sleeper.dto.LonglineResponseDTO;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface LonglineService {

    LonglineResponseDTO create(LonglineRequestDTO dto);

    LonglineResponseDTO update(Long id, LonglineRequestDTO dto);

    LonglineResponseDTO getById(Long id);

    List<LonglineResponseDTO> getAll();

    void delete(Long id);
}
