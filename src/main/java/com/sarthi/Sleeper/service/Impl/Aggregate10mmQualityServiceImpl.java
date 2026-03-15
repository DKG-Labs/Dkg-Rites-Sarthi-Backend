package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.Aggregates.Aggregate10mmQualityRequestDto;
import com.sarthi.Sleeper.dto.Aggregates.Aggregate10mmQualityResponseDto;
import com.sarthi.Sleeper.entity.Aggregate.Aggregate10mmQuality;
import com.sarthi.Sleeper.repository.Aggregate10mmQualityRepository;
import com.sarthi.Sleeper.service.Aggregate10mmQualityService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class Aggregate10mmQualityServiceImpl implements Aggregate10mmQualityService {

    @Autowired
    private Aggregate10mmQualityRepository repository;

    @Override
    public Aggregate10mmQualityResponseDto create(Aggregate10mmQualityRequestDto dto) {
        Aggregate10mmQuality entity = new Aggregate10mmQuality();
        BeanUtils.copyProperties(dto, entity);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setUpdatedDate(LocalDateTime.now());
        entity.setUpdatedBy(dto.getCreatedBy());
        
        Aggregate10mmQuality saved = repository.save(entity);
        return mapToResponseDto(saved);
    }

    @Override
    public Aggregate10mmQualityResponseDto update(Long id, Aggregate10mmQualityRequestDto dto) {
        Aggregate10mmQuality entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        
        BeanUtils.copyProperties(dto, entity, "id", "createdBy", "createdDate");
        entity.setUpdatedDate(LocalDateTime.now());
        entity.setUpdatedBy(dto.getCreatedBy());
        
        Aggregate10mmQuality saved = repository.save(entity);
        return mapToResponseDto(saved);
    }

    @Override
    public Aggregate10mmQualityResponseDto getById(Long id) {
        Aggregate10mmQuality entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        return mapToResponseDto(entity);
    }

    @Override
    public List<Aggregate10mmQualityResponseDto> getAll() {
        return repository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private Aggregate10mmQualityResponseDto mapToResponseDto(Aggregate10mmQuality entity) {
        Aggregate10mmQualityResponseDto dto = new Aggregate10mmQualityResponseDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
