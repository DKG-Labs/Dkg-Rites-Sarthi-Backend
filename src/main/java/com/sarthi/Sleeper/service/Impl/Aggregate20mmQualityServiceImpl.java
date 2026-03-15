package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.Aggregates.Aggregate20mmQualityRequestDto;
import com.sarthi.Sleeper.dto.Aggregates.Aggregate20mmQualityResponseDto;
import com.sarthi.Sleeper.entity.Aggregate.Aggregate20mmQuality;
import com.sarthi.Sleeper.repository.Aggregate20mmQualityRepository;
import com.sarthi.Sleeper.service.Aggregate20mmQualityService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class Aggregate20mmQualityServiceImpl implements Aggregate20mmQualityService {

    @Autowired
    private Aggregate20mmQualityRepository repository;

    @Override
    public Aggregate20mmQualityResponseDto create(Aggregate20mmQualityRequestDto dto) {
        Aggregate20mmQuality entity = new Aggregate20mmQuality();
        BeanUtils.copyProperties(dto, entity);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setUpdatedDate(LocalDateTime.now());
        entity.setUpdatedBy(dto.getCreatedBy());
        
        Aggregate20mmQuality saved = repository.save(entity);
        return mapToResponseDto(saved);
    }

    @Override
    public Aggregate20mmQualityResponseDto update(Long id, Aggregate20mmQualityRequestDto dto) {
        Aggregate20mmQuality entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        
        BeanUtils.copyProperties(dto, entity, "id", "createdBy", "createdDate");
        entity.setUpdatedDate(LocalDateTime.now());
        entity.setUpdatedBy(dto.getCreatedBy());
        
        Aggregate20mmQuality saved = repository.save(entity);
        return mapToResponseDto(saved);
    }

    @Override
    public Aggregate20mmQualityResponseDto getById(Long id) {
        Aggregate20mmQuality entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        return mapToResponseDto(entity);
    }

    @Override
    public List<Aggregate20mmQualityResponseDto> getAll() {
        return repository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private Aggregate20mmQualityResponseDto mapToResponseDto(Aggregate20mmQuality entity) {
        Aggregate20mmQualityResponseDto dto = new Aggregate20mmQualityResponseDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
