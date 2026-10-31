package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.WaterCubeStrengthTestRequestDto;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface WaterCubeStrengthTestService {

    ResponseEntity<?> saveTestResult(WaterCubeStrengthTestRequestDto requestDto);

    ResponseEntity<?> getTestResultsByUser(Long userId);

    ResponseEntity<?> getTestResultById(Long id);

    public List<Map<String, Object>> getAll();

    public com.sarthi.Sleeper.dto.PagedResponse<Map<String, Object>> getPage(
            String plantId, Integer days, Integer page, Integer size);

    public List<Map<String, Object>> getAllLite(String plantId, Integer days);
    public List<Map<String, Object>> getAllLite(String plantId, Integer days,
            java.time.LocalDate fromDate, java.time.LocalDate toDate);
    public ResponseEntity<?> deleteTestResult(Long id);
    public ResponseEntity<?> updateTestResult(Long id, WaterCubeStrengthTestRequestDto requestDto);
    }
