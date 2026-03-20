package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.Cement.CementSpecificSurfaceRequestDto;
import com.sarthi.Sleeper.dto.Cement.CementSpecificSurfaceResponseDto;
import com.sarthi.Sleeper.entity.Cement.CementSpecificSurface;
import com.sarthi.Sleeper.repository.CementSpecificSurfaceRepository;
import com.sarthi.Sleeper.service.CementSpecificSurfaceService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CementSpecificSurfaceServiceImpl implements CementSpecificSurfaceService {

    @Autowired
    private CementSpecificSurfaceRepository repository;

    @Override
    public CementSpecificSurfaceResponseDto create(CementSpecificSurfaceRequestDto dto) {
        CementSpecificSurface entity = new CementSpecificSurface();
        BeanUtils.copyProperties(dto, entity);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setUpdatedDate(LocalDateTime.now());
        entity.setUpdatedBy(dto.getCreatedBy());
        
        CementSpecificSurface saved = repository.save(entity);
        return mapToResponseDto(saved);
    }

    @Override
    public CementSpecificSurfaceResponseDto update(Long id, CementSpecificSurfaceRequestDto dto) {
        CementSpecificSurface entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        
        BeanUtils.copyProperties(dto, entity, "id", "createdBy", "createdDate");
        entity.setUpdatedDate(LocalDateTime.now());
        entity.setUpdatedBy(dto.getCreatedBy()); // Assuming same user for now
        
        CementSpecificSurface saved = repository.save(entity);
        return mapToResponseDto(saved);
    }

    @Override
    public CementSpecificSurfaceResponseDto getById(Long id) {
        CementSpecificSurface entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        return mapToResponseDto(entity);
    }

    @Override
    public List<CementSpecificSurfaceResponseDto> getAll() {
        return repository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public CementSpecificSurfaceResponseDto getByRequestId(Long requestId) {
        return repository.findByRequestId(requestId)
                .map(this::mapToResponseDto)
                .orElse(null);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private CementSpecificSurfaceResponseDto mapToResponseDto(CementSpecificSurface entity) {
        CementSpecificSurfaceResponseDto dto = new CementSpecificSurfaceResponseDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
