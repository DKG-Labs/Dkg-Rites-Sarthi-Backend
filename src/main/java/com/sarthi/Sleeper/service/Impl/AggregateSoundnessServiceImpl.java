package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.Aggregates.AggregateSoundnessRequestDto;
import com.sarthi.Sleeper.dto.Aggregates.AggregateSoundnessResponseDto;
import com.sarthi.Sleeper.entity.Aggregate.AggregateSoundnessTest;
import com.sarthi.Sleeper.repository.AggregateSoundnessRepository;
import com.sarthi.Sleeper.service.AggregateSoundnessService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AggregateSoundnessServiceImpl implements AggregateSoundnessService {

    @Autowired
    private AggregateSoundnessRepository repository;

    @Override
    public AggregateSoundnessResponseDto create(AggregateSoundnessRequestDto dto) {
        AggregateSoundnessTest entity = new AggregateSoundnessTest();
        BeanUtils.copyProperties(dto, entity);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setUpdatedDate(LocalDateTime.now());
        entity.setUpdatedBy(dto.getCreatedBy());
        
        AggregateSoundnessTest saved = repository.save(entity);
        return mapToResponseDto(saved);
    }

    @Override
    public AggregateSoundnessResponseDto update(Long id, AggregateSoundnessRequestDto dto) {
        AggregateSoundnessTest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        
        BeanUtils.copyProperties(dto, entity, "id", "createdBy", "createdDate");
        entity.setUpdatedDate(LocalDateTime.now());
        entity.setUpdatedBy(dto.getCreatedBy());
        
        AggregateSoundnessTest saved = repository.save(entity);
        return mapToResponseDto(saved);
    }

    @Override
    public AggregateSoundnessResponseDto getById(Long id) {
        AggregateSoundnessTest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        return mapToResponseDto(entity);
    }

    @Override
    public List<AggregateSoundnessResponseDto> getAll() {
        return repository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private AggregateSoundnessResponseDto mapToResponseDto(AggregateSoundnessTest entity) {
        AggregateSoundnessResponseDto dto = new AggregateSoundnessResponseDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
