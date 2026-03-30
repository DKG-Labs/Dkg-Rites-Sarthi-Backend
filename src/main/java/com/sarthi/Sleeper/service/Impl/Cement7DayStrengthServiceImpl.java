package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.Cement.Cement7DayStrengthCubeDto;
import com.sarthi.Sleeper.dto.Cement.Cement7DayStrengthRequestDto;
import com.sarthi.Sleeper.dto.Cement.Cement7DayStrengthResponseDto;
import com.sarthi.Sleeper.entity.Cement.Cement7DayStrength;
import com.sarthi.Sleeper.entity.Cement.Cement7DayStrengthCube;
import com.sarthi.Sleeper.repository.Cement7DayStrengthRepository;
import com.sarthi.Sleeper.service.Cement7DayStrengthService;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class Cement7DayStrengthServiceImpl implements Cement7DayStrengthService {

    @Autowired
    private Cement7DayStrengthRepository repository;

    @Override
    public Cement7DayStrengthResponseDto create(Cement7DayStrengthRequestDto dto) {
        Cement7DayStrength entity = new Cement7DayStrength();
        mapRequestToEntity(dto, entity);
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());

        Cement7DayStrength saved = repository.save(entity);
        return mapEntityToResponse(saved);
    }

    @Override
    public Cement7DayStrengthResponseDto update(Long id, Cement7DayStrengthRequestDto dto) {
        Cement7DayStrength entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(new ErrorDetails(
                        AppConstant.ERROR_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_VALIDATION,
                        "Record not found")));

        mapRequestToEntity(dto, entity);
        entity.setUpdatedBy(dto.getCreatedBy()); // Assuming updatedBy passed in same field or use standard
        entity.setUpdatedDate(LocalDateTime.now());

        Cement7DayStrength updated = repository.save(entity);
        return mapEntityToResponse(updated);
    }

    @Override
    public Cement7DayStrengthResponseDto getById(Long id) {
        Cement7DayStrength entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(new ErrorDetails(
                        AppConstant.ERROR_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_VALIDATION,
                        "Record not found")));
        return mapEntityToResponse(entity);
    }

    @Override
    public List<Cement7DayStrengthResponseDto> getAll() {
        return repository.findAll().stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Cement7DayStrengthResponseDto getByRequestId(Long requestId) {
        return repository.findByRequestId(requestId)
                .map(this::mapEntityToResponse)
                .orElse(null);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapRequestToEntity(Cement7DayStrengthRequestDto dto, Cement7DayStrength entity) {
        entity.setTestDate(dto.getTestDate());
        entity.setTypeOfTesting(dto.getTypeOfTesting());
        entity.setRequestId(dto.getRequestId());
        entity.setConsignmentNo(dto.getConsignmentNo());
        entity.setRoomTemp(dto.getRoomTemp());
        entity.setNormalConsistency(dto.getNormalConsistency());
        entity.setWaterRequired(dto.getWaterRequired());
        entity.setMinStrength(dto.getMinStrength());
        entity.setCubeResult(dto.getCubeResult());
        entity.setSoundness(dto.getSoundness());
        entity.setSoundnessResult(dto.getSoundnessResult());
        entity.setShift(dto.getShift());
        entity.setLineNo(dto.getLineNo());
        entity.setDateOfInspection(dto.getDateOfInspection());

        if (entity.getCubes() == null) {
            entity.setCubes(new ArrayList<>());
        } else {
            entity.getCubes().clear();
        }

        if (dto.getCubes() != null) {
            for (Cement7DayStrengthCubeDto cDto : dto.getCubes()) {
                Cement7DayStrengthCube cube = new Cement7DayStrengthCube();
                cube.setCastDate(cDto.getCastDate());
                cube.setCastTime(cDto.getCastTime());
                cube.setTestDate(cDto.getTestDate());
                cube.setTestTime(cDto.getTestTime());
                cube.setLoadKn(cDto.getLoadKn());
                cube.setStrengthNmm2(cDto.getStrengthNmm2());
                cube.setCement7DayStrength(entity);
                entity.getCubes().add(cube);
            }
        }
    }

    private Cement7DayStrengthResponseDto mapEntityToResponse(Cement7DayStrength entity) {
        Cement7DayStrengthResponseDto response = new Cement7DayStrengthResponseDto();
        response.setId(entity.getId());
        response.setTestDate(entity.getTestDate());
        response.setTypeOfTesting(entity.getTypeOfTesting());
        response.setRequestId(entity.getRequestId());
        response.setConsignmentNo(entity.getConsignmentNo());
        response.setRoomTemp(entity.getRoomTemp());
        response.setNormalConsistency(entity.getNormalConsistency());
        response.setWaterRequired(entity.getWaterRequired());
        response.setMinStrength(entity.getMinStrength());
        response.setCubeResult(entity.getCubeResult());
        response.setSoundness(entity.getSoundness());
        response.setSoundnessResult(entity.getSoundnessResult());
        response.setShift(entity.getShift());
        response.setLineNo(entity.getLineNo());
        response.setDateOfInspection(entity.getDateOfInspection());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setUpdatedDate(entity.getUpdatedDate());

        if (entity.getCubes() != null) {
            response.setCubes(entity.getCubes().stream().map(cube -> {
                Cement7DayStrengthCubeDto cDto = new Cement7DayStrengthCubeDto();
                cDto.setId(cube.getId());
                cDto.setCastDate(cube.getCastDate());
                cDto.setCastTime(cube.getCastTime());
                cDto.setTestDate(cube.getTestDate());
                cDto.setTestTime(cube.getTestTime());
                cDto.setLoadKn(cube.getLoadKn());
                cDto.setStrengthNmm2(cube.getStrengthNmm2());
                return cDto;
            }).collect(Collectors.toList()));
        }
        return response;
    }
}
