package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.HtsWire.HtsWireDailyTestRequestDto;
import com.sarthi.Sleeper.dto.HtsWire.HtsWireDailyTestResponseDto;
import com.sarthi.Sleeper.entity.HtsWireDailyTest;
import com.sarthi.Sleeper.repository.HtsWireDailyTestRepository;
import com.sarthi.Sleeper.service.HtsWireDailyTestService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HtsWireDailyTestServiceImpl implements HtsWireDailyTestService {

    @Autowired
    private HtsWireDailyTestRepository repository;

    @Override
    public HtsWireDailyTestResponseDto create(HtsWireDailyTestRequestDto dto) {
        HtsWireDailyTest entity = new HtsWireDailyTest();
        BeanUtils.copyProperties(dto, entity);
        entity = repository.save(entity);
        return convertToDto(entity);
    }

    @Override
    public HtsWireDailyTestResponseDto update(Long id, HtsWireDailyTestRequestDto dto) {
        HtsWireDailyTest entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Test not found"));
        BeanUtils.copyProperties(dto, entity, "id", "createdDate", "createdBy");
        entity.setUpdatedBy(dto.getCreatedBy());
        entity = repository.save(entity);
        return convertToDto(entity);
    }

    @Override
    public HtsWireDailyTestResponseDto getById(Long id) {
        return convertToDto(repository.findById(id).orElseThrow(() -> new RuntimeException("Test not found")));
    }

    @Override
    public HtsWireDailyTestResponseDto getByRequestId(Long requestId) {
        return repository.findByRequestId(requestId)
                .map(this::convertToDto)
                .orElse(null);
    }

    @Override
    public List<HtsWireDailyTestResponseDto> getAll() {
        return repository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private HtsWireDailyTestResponseDto convertToDto(HtsWireDailyTest entity) {
        HtsWireDailyTestResponseDto dto = new HtsWireDailyTestResponseDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
