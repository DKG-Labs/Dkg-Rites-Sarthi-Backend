package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.StressBenchRequestDto;
import com.sarthi.Sleeper.dto.StressBenchResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface StressBenchMasterService {

   public StressBenchResponseDto createBench(StressBenchRequestDto dto);

   public StressBenchResponseDto updateBench(Long id, StressBenchRequestDto dto);

   public StressBenchResponseDto getBenchById(Long id);

   public List<StressBenchResponseDto> getAllBenches();

   public void deleteBench(Long id);

}
