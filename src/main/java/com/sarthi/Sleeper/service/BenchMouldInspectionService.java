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

    List<BenchMouldInspectionResponseDto> getAllLite(Integer days);

    com.sarthi.Sleeper.dto.PagedResponse<BenchMouldInspectionResponseDto> getPage(
            Integer days, Integer page, Integer size);

    void delete(Long id);
}
