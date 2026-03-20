package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.AdmixtureTestRequestDto;
import com.sarthi.Sleeper.dto.AdmixtureTestResponseDto;
import com.sarthi.Sleeper.entity.AdmixtureTest;
import com.sarthi.Sleeper.repository.AdmixtureTestRepository;
import com.sarthi.Sleeper.service.AdmixtureTestService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdmixtureTestServiceImpl implements AdmixtureTestService {

    @Autowired
    private AdmixtureTestRepository repository;

    @Override
    public AdmixtureTestResponseDto create(AdmixtureTestRequestDto dto) {
        AdmixtureTest entity = new AdmixtureTest();
        BeanUtils.copyProperties(dto, entity);
        entity = repository.save(entity);
        return convertToDto(entity);
    }

    @Override
    public AdmixtureTestResponseDto update(Long id, AdmixtureTestRequestDto dto) {
        AdmixtureTest entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Test not found"));
        BeanUtils.copyProperties(dto, entity, "id", "createdDate", "createdBy");
        entity.setUpdatedBy(dto.getCreatedBy());
        entity = repository.save(entity);
        return convertToDto(entity);
    }

    @Override
    public AdmixtureTestResponseDto getById(Long id) {
        return convertToDto(repository.findById(id).orElseThrow(() -> new RuntimeException("Test not found")));
    }

    @Override
    public AdmixtureTestResponseDto getByRequestId(Long requestId) {
        return repository.findByRequestId(requestId)
                .map(this::convertToDto)
                .orElse(null);
    }

    @Override
    public List<AdmixtureTestResponseDto> getAll() {
        return repository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private AdmixtureTestResponseDto convertToDto(AdmixtureTest entity) {
        AdmixtureTestResponseDto dto = new AdmixtureTestResponseDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
