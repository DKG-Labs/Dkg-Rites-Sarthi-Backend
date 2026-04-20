package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.WaterQualityTestDto;
import com.sarthi.Sleeper.entity.WaterQualityTest;
import com.sarthi.Sleeper.repository.WaterQualityTestRepository;
import com.sarthi.Sleeper.service.WaterQualityTestService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WaterQualityTestServiceImpl implements WaterQualityTestService {

    @Autowired
    private WaterQualityTestRepository repository;

    @Override
    public WaterQualityTestDto create(WaterQualityTestDto dto) {
        WaterQualityTest entity = new WaterQualityTest();
        BeanUtils.copyProperties(dto, entity);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setUpdatedDate(LocalDateTime.now());
        entity.setUpdatedBy(dto.getCreatedBy());

        WaterQualityTest saved = repository.save(entity);
        return mapToDto(saved);
    }

    @Override
    public WaterQualityTestDto update(Long id, WaterQualityTestDto dto) {
        WaterQualityTest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        BeanUtils.copyProperties(dto, entity, "id", "createdBy", "createdDate");
        entity.setUpdatedDate(LocalDateTime.now());
        entity.setUpdatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : dto.getUpdatedBy());

        WaterQualityTest saved = repository.save(entity);
        return mapToDto(saved);
    }

    @Override
    public WaterQualityTestDto getById(Long id) {
        WaterQualityTest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        return mapToDto(entity);
    }

    @Override
    public List<WaterQualityTestDto> getAll() {
        return repository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<WaterQualityTestDto> getByUserId(Integer userId) {
        return repository.findByCreatedByOrderByTestDateDesc(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private WaterQualityTestDto mapToDto(WaterQualityTest entity) {
        WaterQualityTestDto dto = new WaterQualityTestDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
