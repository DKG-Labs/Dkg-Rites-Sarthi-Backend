package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.Aggregates.AggregateFlakinessRequestDto;
import com.sarthi.Sleeper.dto.Aggregates.AggregateFlakinessResponseDto;
import com.sarthi.Sleeper.dto.Aggregates.AggregateFlakinessRowDto;
import com.sarthi.Sleeper.entity.Aggregate.AggregateFlakinessRow;
import com.sarthi.Sleeper.entity.Aggregate.AggregateFlakinessTest;
import com.sarthi.Sleeper.repository.AggregateFlakinessRepository;
import com.sarthi.Sleeper.service.AggregateFlakinessService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AggregateFlakinessServiceImpl implements AggregateFlakinessService {

    @Autowired
    private AggregateFlakinessRepository repository;

    @Override
    @Transactional
    public AggregateFlakinessResponseDto create(AggregateFlakinessRequestDto dto) {
        AggregateFlakinessTest entity = new AggregateFlakinessTest();
        BeanUtils.copyProperties(dto, entity, "observations");
        
        if (dto.getObservations() != null) {
            List<AggregateFlakinessRow> rows = dto.getObservations().stream().map(rowDto -> {
                AggregateFlakinessRow row = new AggregateFlakinessRow();
                BeanUtils.copyProperties(rowDto, row);
                row.setFlakinessTest(entity);
                return row;
            }).collect(Collectors.toList());
            entity.setObservations(rows);
        }

        entity.setCreatedDate(LocalDateTime.now());
        entity.setUpdatedDate(LocalDateTime.now());
        entity.setUpdatedBy(dto.getCreatedBy());
        
        AggregateFlakinessTest saved = repository.save(entity);
        return mapToResponseDto(saved);
    }

    @Override
    @Transactional
    public AggregateFlakinessResponseDto update(Long id, AggregateFlakinessRequestDto dto) {
        AggregateFlakinessTest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        
        BeanUtils.copyProperties(dto, entity, "id", "createdBy", "createdDate", "observations");
        
        entity.getObservations().clear();
        if (dto.getObservations() != null) {
            List<AggregateFlakinessRow> rows = dto.getObservations().stream().map(rowDto -> {
                AggregateFlakinessRow row = new AggregateFlakinessRow();
                BeanUtils.copyProperties(rowDto, row);
                row.setFlakinessTest(entity);
                return row;
            }).collect(Collectors.toList());
            entity.getObservations().addAll(rows);
        }

        entity.setUpdatedDate(LocalDateTime.now());
        entity.setUpdatedBy(dto.getCreatedBy());
        
        AggregateFlakinessTest saved = repository.save(entity);
        return mapToResponseDto(saved);
    }

    @Override
    public AggregateFlakinessResponseDto getById(Long id) {
        AggregateFlakinessTest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        return mapToResponseDto(entity);
    }

    @Override
    public List<AggregateFlakinessResponseDto> getAll() {
        return repository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private AggregateFlakinessResponseDto mapToResponseDto(AggregateFlakinessTest entity) {
        AggregateFlakinessResponseDto dto = new AggregateFlakinessResponseDto();
        BeanUtils.copyProperties(entity, dto, "observations");
        
        if (entity.getObservations() != null) {
            List<AggregateFlakinessRowDto> rowDtos = entity.getObservations().stream().map(row -> {
                AggregateFlakinessRowDto rd = new AggregateFlakinessRowDto();
                BeanUtils.copyProperties(row, rd);
                return rd;
            }).collect(Collectors.toList());
            dto.setObservations(rowDtos);
        }
        
        return dto;
    }
}
