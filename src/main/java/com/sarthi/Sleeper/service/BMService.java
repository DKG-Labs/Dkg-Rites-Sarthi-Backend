package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.BenchMouldLongStrssDtos.BMRequestDTO;
import com.sarthi.Sleeper.dto.BenchMouldLongStrssDtos.BMResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BMService {

    BMResponseDTO create(BMRequestDTO request);

    BMResponseDTO getById(Long id);

    List<BMResponseDTO> getAll();

    BMResponseDTO update(Long id, BMRequestDTO request);

    void delete(Long id);
}
