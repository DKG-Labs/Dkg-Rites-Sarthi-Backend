package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.Aggregates.AggregateGranulometricRequestDto;
import com.sarthi.Sleeper.dto.Aggregates.AggregateGranulometricResponseDto;
import com.sarthi.Sleeper.dto.Aggregates.AggregateGranulometricRowDto;
import com.sarthi.Sleeper.entity.Aggregate.AggregateGranulometricRow;
import com.sarthi.Sleeper.entity.Aggregate.AggregateGranulometricTest;
import com.sarthi.Sleeper.repository.AggregateGranulometricRepository;
import com.sarthi.Sleeper.service.AggregateGranulometricService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AggregateGranulometricServiceImpl implements AggregateGranulometricService {

    @Autowired
    private AggregateGranulometricRepository repository;

    @Override
    @Transactional
    public AggregateGranulometricResponseDto create(AggregateGranulometricRequestDto dto) {
        AggregateGranulometricTest entity = new AggregateGranulometricTest();
        BeanUtils.copyProperties(dto, entity, "observations");
        
        if (dto.getObservations() != null) {
            List<AggregateGranulometricRow> rows = dto.getObservations().stream().map(rowDto -> {
                AggregateGranulometricRow row = new AggregateGranulometricRow();
                BeanUtils.copyProperties(rowDto, row);
                row.setGranulometricTest(entity);
                return row;
            }).collect(Collectors.toList());
            entity.setObservations(rows);
        }

        entity.setCreatedDate(LocalDateTime.now());
        entity.setUpdatedDate(LocalDateTime.now());
        entity.setUpdatedBy(dto.getCreatedBy());
        
        AggregateGranulometricTest saved = repository.save(entity);
        return mapToResponseDto(saved);
    }

    @Override
    @Transactional
    public AggregateGranulometricResponseDto update(Long id, AggregateGranulometricRequestDto dto) {
        AggregateGranulometricTest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        
        BeanUtils.copyProperties(dto, entity, "id", "createdBy", "createdDate", "observations");
        
        entity.getObservations().clear();
        if (dto.getObservations() != null) {
            List<AggregateGranulometricRow> rows = dto.getObservations().stream().map(rowDto -> {
                AggregateGranulometricRow row = new AggregateGranulometricRow();
                BeanUtils.copyProperties(rowDto, row);
                row.setGranulometricTest(entity);
                return row;
            }).collect(Collectors.toList());
            entity.getObservations().addAll(rows);
        }

        entity.setUpdatedDate(LocalDateTime.now());
        entity.setUpdatedBy(dto.getCreatedBy());
        
        AggregateGranulometricTest saved = repository.save(entity);
        return mapToResponseDto(saved);
    }

    @Override
    public AggregateGranulometricResponseDto getById(Long id) {
        AggregateGranulometricTest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        return mapToResponseDto(entity);
    }

    @Override
    public List<AggregateGranulometricResponseDto> getAll() {
        return repository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private AggregateGranulometricResponseDto mapToResponseDto(AggregateGranulometricTest entity) {
        AggregateGranulometricResponseDto dto = new AggregateGranulometricResponseDto();
        BeanUtils.copyProperties(entity, dto, "observations");
        
        if (entity.getObservations() != null) {
            List<AggregateGranulometricRowDto> rowDtos = entity.getObservations().stream().map(row -> {
                AggregateGranulometricRowDto rd = new AggregateGranulometricRowDto();
                BeanUtils.copyProperties(row, rd);
                return rd;
            }).collect(Collectors.toList());
            dto.setObservations(rowDtos);
        }
        
        return dto;
    }
}
