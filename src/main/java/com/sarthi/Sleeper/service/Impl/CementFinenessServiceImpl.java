package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.Cement.CementFinenessRequestDto;
import com.sarthi.Sleeper.dto.Cement.CementFinenessResponseDto;
import com.sarthi.Sleeper.entity.Cement.CementFinenessTest;
import com.sarthi.Sleeper.repository.CementFinenessRepository;
import com.sarthi.Sleeper.service.CementFinenessService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CementFinenessServiceImpl implements CementFinenessService {

    @Autowired
    private CementFinenessRepository repository;

    @Override
    public CementFinenessResponseDto create(CementFinenessRequestDto dto) {
        CementFinenessTest entity = new CementFinenessTest();
        BeanUtils.copyProperties(dto, entity);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setUpdatedDate(LocalDateTime.now());
        entity.setUpdatedBy(dto.getCreatedBy());
        
        CementFinenessTest saved = repository.save(entity);
        return mapToResponseDto(saved);
    }

    @Override
    public CementFinenessResponseDto update(Long id, CementFinenessRequestDto dto) {
        CementFinenessTest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        
        BeanUtils.copyProperties(dto, entity, "id", "createdBy", "createdDate");
        entity.setUpdatedDate(LocalDateTime.now());
        entity.setUpdatedBy(dto.getCreatedBy());
        
        CementFinenessTest saved = repository.save(entity);
        return mapToResponseDto(saved);
    }

    @Override
    public CementFinenessResponseDto getById(Long id) {
        CementFinenessTest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        return mapToResponseDto(entity);
    }

    @Override
    public List<CementFinenessResponseDto> getAll() {
        return repository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public CementFinenessResponseDto getByRequestId(Long requestId) {
        return repository.findByRequestId(requestId)
                .map(this::mapToResponseDto)
                .orElse(null);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private CementFinenessResponseDto mapToResponseDto(CementFinenessTest entity) {
        CementFinenessResponseDto dto = new CementFinenessResponseDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
