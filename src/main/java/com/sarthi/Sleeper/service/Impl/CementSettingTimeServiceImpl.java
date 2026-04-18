package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.Cement.CementSettingTimeRequestDto;
import com.sarthi.Sleeper.dto.Cement.CementSettingTimeResponseDto;
import com.sarthi.Sleeper.dto.Cement.CementSettingTimeObservationDto;
import com.sarthi.Sleeper.entity.Cement.CementSettingTime;
import com.sarthi.Sleeper.entity.Cement.CementSettingTimeObservation;
import com.sarthi.Sleeper.repository.CementSettingTimeRepository;
import com.sarthi.Sleeper.service.CementSettingTimeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CementSettingTimeServiceImpl implements CementSettingTimeService {

    @Autowired
    private CementSettingTimeRepository repository;

    @Override
    @Transactional
    public CementSettingTimeResponseDto create(CementSettingTimeRequestDto dto) {
        CementSettingTime entity = new CementSettingTime();
        BeanUtils.copyProperties(dto, entity, "observations");
        
        entity.setCreatedDate(LocalDateTime.now());
        entity.setUpdatedDate(LocalDateTime.now());
        entity.setUpdatedBy(dto.getCreatedBy());

        if (dto.getObservations() != null) {
            List<CementSettingTimeObservation> observations = dto.getObservations().stream().map(obsDto -> {
                CementSettingTimeObservation obs = new CementSettingTimeObservation();
                BeanUtils.copyProperties(obsDto, obs);
                obs.setCementSettingTime(entity);
                return obs;
            }).collect(Collectors.toList());
            entity.setObservations(observations);
        }

        CementSettingTime saved = repository.save(entity);
        return mapToResponseDto(saved);
    }

    @Override
    @Transactional
    public CementSettingTimeResponseDto update(Long id, CementSettingTimeRequestDto dto) {
        CementSettingTime entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        BeanUtils.copyProperties(dto, entity, "id", "observations", "createdBy", "createdDate");
        entity.setUpdatedDate(LocalDateTime.now());
        entity.setUpdatedBy(dto.getCreatedBy());

        // Update observations
        entity.getObservations().clear();
        if (dto.getObservations() != null) {
            List<CementSettingTimeObservation> observations = dto.getObservations().stream().map(obsDto -> {
                CementSettingTimeObservation obs = new CementSettingTimeObservation();
                BeanUtils.copyProperties(obsDto, obs);
                obs.setCementSettingTime(entity);
                return obs;
            }).collect(Collectors.toList());
            entity.getObservations().addAll(observations);
        }

        CementSettingTime saved = repository.save(entity);
        return mapToResponseDto(saved);
    }

    @Override
    public CementSettingTimeResponseDto getById(Long id) {
        CementSettingTime entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        return mapToResponseDto(entity);
    }

    @Override
    public List<CementSettingTimeResponseDto> getAll() {
        return repository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CementSettingTimeResponseDto> getPeriodic() {
        return repository.findAllByTypeOfTesting("Periodic").stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public CementSettingTimeResponseDto getByRequestId(Long requestId) {
        return repository.findByRequestId(requestId)
                .map(this::mapToResponseDto)
                .orElse(null);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private CementSettingTimeResponseDto mapToResponseDto(CementSettingTime entity) {
        CementSettingTimeResponseDto dto = new CementSettingTimeResponseDto();
        BeanUtils.copyProperties(entity, dto, "observations");
        
        if (entity.getObservations() != null) {
            List<CementSettingTimeObservationDto> obsDtos = entity.getObservations().stream().map(obs -> {
                CementSettingTimeObservationDto obsDto = new CementSettingTimeObservationDto();
                BeanUtils.copyProperties(obs, obsDto);
                return obsDto;
            }).collect(Collectors.toList());
            dto.setObservations(obsDtos);
        }
        
        return dto;
    }
}
