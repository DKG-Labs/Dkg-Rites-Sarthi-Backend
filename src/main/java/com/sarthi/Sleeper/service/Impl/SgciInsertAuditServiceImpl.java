package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.SgciInventory.SgciInsertAuditAuditRequestDto;
import com.sarthi.Sleeper.dto.SgciInventory.SgciInsertAuditAuditResponseDto;
import com.sarthi.Sleeper.dto.SgciInventory.SgciInsertReadingDto;
import com.sarthi.Sleeper.entity.SgciInsertAudit;
import com.sarthi.Sleeper.entity.SgciInsertReading;
import com.sarthi.Sleeper.repository.SgciInsertAuditRepository;
import com.sarthi.Sleeper.service.SgciInsertAuditService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SgciInsertAuditServiceImpl implements SgciInsertAuditService {

    @Autowired
    private SgciInsertAuditRepository repository;

    @Override
    public SgciInsertAuditAuditResponseDto create(SgciInsertAuditAuditRequestDto dto) {
        SgciInsertAudit entity = new SgciInsertAudit();
        BeanUtils.copyProperties(dto, entity, "readings");
        
        if (dto.getReadings() != null) {
            for (SgciInsertReadingDto rDto : dto.getReadings()) {
                SgciInsertReading reading = new SgciInsertReading();
                BeanUtils.copyProperties(rDto, reading);
                reading.setAudit(entity);
                entity.getReadings().add(reading);
            }
        }
        
        entity = repository.save(entity);
        return convertToDto(entity);
    }

    @Override
    public SgciInsertAuditAuditResponseDto update(Long id, SgciInsertAuditAuditRequestDto dto) {
        SgciInsertAudit entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Audit not found"));
        BeanUtils.copyProperties(dto, entity, "id", "createdDate", "createdBy", "readings");
        entity.setUpdatedBy(dto.getCreatedBy());
        
        // Update readings
        entity.getReadings().clear();
        if (dto.getReadings() != null) {
            for (SgciInsertReadingDto rDto : dto.getReadings()) {
                SgciInsertReading reading = new SgciInsertReading();
                BeanUtils.copyProperties(rDto, reading);
                reading.setAudit(entity);
                entity.getReadings().add(reading);
            }
        }
        
        entity = repository.save(entity);
        return convertToDto(entity);
    }

    @Override
    public SgciInsertAuditAuditResponseDto getById(Long id) {
        return convertToDto(repository.findById(id).orElseThrow(() -> new RuntimeException("Audit not found")));
    }

    @Override
    public List<SgciInsertAuditAuditResponseDto> getAll() {
        return repository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private SgciInsertAuditAuditResponseDto convertToDto(SgciInsertAudit entity) {
        SgciInsertAuditAuditResponseDto dto = new SgciInsertAuditAuditResponseDto();
        BeanUtils.copyProperties(entity, dto, "readings");
        
        if (entity.getReadings() != null) {
            dto.setReadings(entity.getReadings().stream().map(r -> {
                SgciInsertReadingDto rDto = new SgciInsertReadingDto();
                BeanUtils.copyProperties(r, rDto);
                return rDto;
            }).collect(Collectors.toList()));
        }
        
        return dto;
    }
}
