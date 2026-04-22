package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.EtDtos.EpoxyTreatedSleeperRequestDTO;
import com.sarthi.Sleeper.dto.EtDtos.EpoxyTreatedSleeperResponseDTO;

import com.sarthi.Sleeper.dto.EtDtos.EtBatchSummaryResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EpoxyTreatedSleeperService {

    EpoxyTreatedSleeperResponseDTO create(EpoxyTreatedSleeperRequestDTO dto);

    EpoxyTreatedSleeperResponseDTO update(Long id, EpoxyTreatedSleeperRequestDTO dto);

    EpoxyTreatedSleeperResponseDTO getById(Long id);

    List<EpoxyTreatedSleeperResponseDTO> getAll();

    void delete(Long id);

    public List<EtBatchSummaryResponseDTO> getAllBatchWiseEtSummary();

}
