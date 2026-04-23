package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.MainIeInspectionDtos.SleeperInspectionBatchDetailDTO;
import com.sarthi.Sleeper.dto.MainIeInspectionDtos.SleeperInspectionCallSummaryDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MainIeInspectionService {

    public SleeperInspectionCallSummaryDTO getInspectionCallSummary(String callNo);

    public List<SleeperInspectionBatchDetailDTO> getBatchWiseDetails(String callNo);
    }
