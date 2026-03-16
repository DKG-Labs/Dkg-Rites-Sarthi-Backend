package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.WaterCubeStrengthTestRequestDto;
import org.springframework.http.ResponseEntity;

public interface WaterCubeStrengthTestService {

    ResponseEntity<?> saveTestResult(WaterCubeStrengthTestRequestDto requestDto);

    ResponseEntity<?> getTestResultsByUser(Long userId);

    ResponseEntity<?> getTestResultById(Long id);
}
