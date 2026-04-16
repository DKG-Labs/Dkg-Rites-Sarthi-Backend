package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.BatchIdNumberDto;
import com.sarthi.Sleeper.dto.BatchWeighmentDtos.BatchWeighmentRequestDto;
import com.sarthi.Sleeper.dto.BatchWeighmentDtos.BatchWeighmentResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BatchWeighmentService {

    BatchWeighmentResponseDto create(BatchWeighmentRequestDto dto);

    BatchWeighmentResponseDto update(Long id, BatchWeighmentRequestDto dto);

    BatchWeighmentResponseDto getById(Long id);

    List<BatchWeighmentResponseDto> getAll();

    void delete(Long id);

    public List<BatchWeighmentResponseDto> getRecordsByDate(
            String plantId,
            String vendorCode,
            String shift,
            int createdBy,
            String date);

    public List<BatchIdNumberDto> getAllBatchIdsAndNumbers();
}
