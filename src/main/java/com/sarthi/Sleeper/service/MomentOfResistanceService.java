package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.MomentOfResistanceRequestDTO;
import com.sarthi.Sleeper.dto.MomentOfResistanceResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MomentOfResistanceService {

    MomentOfResistanceResponseDTO create(MomentOfResistanceRequestDTO dto);

    MomentOfResistanceResponseDTO getById(Long id);

    MomentOfResistanceResponseDTO update(Long id, MomentOfResistanceRequestDTO dto);

    List<MomentOfResistanceResponseDTO> getAll();

    void delete(Long id);

    public List<MomentOfResistanceResponseDTO> getRecordsByDate(
            String plantId,
            String vendorCode,
            String shift,
            int createdBy,
            String date);
}
