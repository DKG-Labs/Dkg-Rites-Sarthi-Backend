package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.BenchMouldDtos.BenchMouldInspectionRequestDto;
import com.sarthi.Sleeper.dto.BenchMouldDtos.BenchMouldInspectionResponseDto;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public interface BenchMouldInspectionService {

    BenchMouldInspectionResponseDto create(
            BenchMouldInspectionRequestDto dto);

    BenchMouldInspectionResponseDto update(
            Long id,
            BenchMouldInspectionRequestDto dto);

    BenchMouldInspectionResponseDto getById(Long id);

    List<BenchMouldInspectionResponseDto> getAll();

    void delete(Long id);
}
