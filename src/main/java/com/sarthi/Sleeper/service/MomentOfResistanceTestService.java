package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.MomentOfResistanceTestRequestDTO;
import com.sarthi.Sleeper.dto.MomentOfResistanceTestResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MomentOfResistanceTestService {

    MomentOfResistanceTestResponseDTO create(MomentOfResistanceTestRequestDTO dto);

    MomentOfResistanceTestResponseDTO getById(Long id);

    MomentOfResistanceTestResponseDTO update(Long id, MomentOfResistanceTestRequestDTO dto);

    List<MomentOfResistanceTestResponseDTO> getAll();

    void delete(Long id);

    public List<MomentOfResistanceTestResponseDTO> getRecordsByDate(
            String plantId,
            String vendorCode,
            String shift,
            int createdBy,
            String date);
}
