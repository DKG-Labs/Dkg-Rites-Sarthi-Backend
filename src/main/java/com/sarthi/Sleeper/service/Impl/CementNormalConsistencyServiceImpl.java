package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.Cement.CementNormalConsistencyObservationDto;
import com.sarthi.Sleeper.dto.Cement.CementNormalConsistencyRequestDto;
import com.sarthi.Sleeper.dto.Cement.CementNormalConsistencyResponseDto;
import com.sarthi.Sleeper.entity.Cement.CementNormalConsistency;
import com.sarthi.Sleeper.entity.Cement.CementNormalConsistencyObservation;
import com.sarthi.Sleeper.repository.CementNormalConsistencyRepository;
import com.sarthi.Sleeper.service.CementNormalConsistencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CementNormalConsistencyServiceImpl implements CementNormalConsistencyService {

    @Autowired
    private CementNormalConsistencyRepository repository;

    @Override
    @Transactional
    public CementNormalConsistencyResponseDto create(CementNormalConsistencyRequestDto dto) {
        CementNormalConsistency entity = mapToEntity(dto);
        entity.setCreatedDate(LocalDateTime.now());
        CementNormalConsistency saved = repository.save(entity);
        return mapToResponseDto(saved);
    }

    @Override
    @Transactional
    public CementNormalConsistencyResponseDto update(Long id, CementNormalConsistencyRequestDto dto) {
        CementNormalConsistency existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        
        updateEntity(existing, dto);
        existing.setUpdatedDate(LocalDateTime.now());
        existing.setUpdatedBy(dto.getCreatedBy());
        
        CementNormalConsistency saved = repository.save(existing);
        return mapToResponseDto(saved);
    }

    @Override
    public CementNormalConsistencyResponseDto getById(Long id) {
        return repository.findById(id)
                .map(this::mapToResponseDto)
                .orElseThrow(() -> new RuntimeException("Record not found"));
    }

    @Override
    public List<CementNormalConsistencyResponseDto> getAll() {
        return repository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private CementNormalConsistency mapToEntity(CementNormalConsistencyRequestDto dto) {
        CementNormalConsistency entity = new CementNormalConsistency();
        updateEntity(entity, dto);
        entity.setCreatedBy(dto.getCreatedBy());
        return entity;
    }

    private void updateEntity(CementNormalConsistency entity, CementNormalConsistencyRequestDto dto) {
        entity.setTestDate(dto.getTestDate());
        entity.setTypeOfTesting(dto.getTypeOfTesting());
        entity.setConsignmentNo(dto.getConsignmentNo());
        entity.setRoomTemp(dto.getRoomTemp());
        entity.setSampleWeight(dto.getSampleWeight());
        entity.setShift(dto.getShift());
        entity.setLineNo(dto.getLineNo());
        entity.setDateOfInspection(dto.getDateOfInspection());

        if (entity.getObservations() != null) {
            entity.getObservations().clear();
        } else {
            entity.setObservations(new ArrayList<>());
        }

        if (dto.getObservations() != null) {
            for (CementNormalConsistencyObservationDto obsDto : dto.getObservations()) {
                CementNormalConsistencyObservation obs = new CementNormalConsistencyObservation();
                obs.setPercentWaterAdded(obsDto.getPercentWaterAdded());
                obs.setVolume(obsDto.getVolume());
                obs.setTimeOfAdding(obsDto.getTimeOfAdding());
                obs.setReadingTime(obsDto.getReadingTime());
                obs.setNeedleReading(obsDto.getNeedleReading());
                obs.setCementNormalConsistency(entity);
                entity.getObservations().add(obs);
            }
        }
    }

    private CementNormalConsistencyResponseDto mapToResponseDto(CementNormalConsistency entity) {
        CementNormalConsistencyResponseDto dto = new CementNormalConsistencyResponseDto();
        dto.setId(entity.getId());
        dto.setTestDate(entity.getTestDate());
        dto.setTypeOfTesting(entity.getTypeOfTesting());
        dto.setConsignmentNo(entity.getConsignmentNo());
        dto.setRoomTemp(entity.getRoomTemp());
        dto.setSampleWeight(entity.getSampleWeight());
        dto.setShift(entity.getShift());
        dto.setLineNo(entity.getLineNo());
        dto.setDateOfInspection(entity.getDateOfInspection());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());

        if (entity.getObservations() != null) {
            dto.setObservations(entity.getObservations().stream().map(obs -> {
                CementNormalConsistencyObservationDto obsDto = new CementNormalConsistencyObservationDto();
                obsDto.setPercentWaterAdded(obs.getPercentWaterAdded());
                obsDto.setVolume(obs.getVolume());
                obsDto.setTimeOfAdding(obs.getTimeOfAdding());
                obsDto.setReadingTime(obs.getReadingTime());
                obsDto.setNeedleReading(obs.getNeedleReading());
                return obsDto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }
}
