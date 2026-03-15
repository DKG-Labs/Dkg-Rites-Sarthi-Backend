package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.BatchInspectionDetailDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.BatchTestingListResponseDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.InspectionSaveRequestDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProductionFinalInspectionService {
    void saveInspection(InspectionSaveRequestDto dto);

    public List<BatchTestingListResponseDto> getAllBatchTesting();

    public BatchInspectionDetailDto getBatchInspection(Long batchId);
}
