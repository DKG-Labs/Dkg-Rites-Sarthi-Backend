package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.StressBenchRequestDto;
import com.sarthi.Sleeper.dto.StressBenchResponseDto;
import com.sarthi.Sleeper.entity.StressBenchMaster;
import com.sarthi.Sleeper.repository.StressBenchMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class StressBenchServiceImpl  {

  /*  @Autowired
    private StressBenchMasterRepository repository;

    @Override
    public StressBenchResponseDto createBench(StressBenchRequestDto dto, Long userId) {

        StressBenchMaster entity = new StressBenchMaster();

        mapDtoToEntity(entity, dto);

        entity.setCre(userId);
        entity.setCreatedDate(LocalDateTime.now());

        StressBenchMaster saved = repository.save(entity);

        return mapToResponse(saved);
    }


    @Override
    public StressBenchResponseDto updateBench(Long id, StressBenchRequestDto dto, Long userId) {

        StressBenchMaster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bench not found with id: " + id));

        mapDtoToEntity(entity, dto);

        entity.setUpdatedBy(userId);
        entity.setUpdatedDate(LocalDateTime.now());

        StressBenchMaster updated = repository.save(entity);

        return mapToResponse(updated);
    }


    @Override
    public StressBenchResponseDto getBenchById(Long id) {

        StressBenchMaster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bench not found with id: " + id));

        return mapToResponse(entity);
    }


    @Override
    public List<StressBenchResponseDto> getAllBenches() {

        List<StressBenchMaster> list = repository.findAll();

        return list.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    @Override
    public void deleteBench(Long id) {

        StressBenchMaster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bench not found with id: " + id));

        repository.delete(entity);
    }


    private void mapDtoToEntity(StressBenchMaster entity, StressBenchRequestDto dto) {

        entity.setEntryType(dto.getEntryType());
        entity.setSleeperCategory(dto.getSleeperCategory());
        entity.setMouldsPerBench(dto.getMouldsPerBench());

        if ("RANGE".equalsIgnoreCase(dto.getEntryType())) {

            entity.setBenchFrom(dto.getBenchFrom());
            entity.setBenchTo(dto.getBenchTo());

            int noOfBenches = dto.getBenchTo() - dto.getBenchFrom() + 1;
            entity.setNoOfBenches(noOfBenches);

            entity.setBenchNo(null);

        } else {

            entity.setBenchNo(dto.getBenchNo());
            entity.setNoOfBenches(1);

            entity.setBenchFrom(null);
            entity.setBenchTo(null);
        }
    }


    private StressBenchResponseDto mapToResponse(StressBenchMaster entity) {

        StressBenchResponseDto dto = new StressBenchResponseDto();

        dto.setId(entity.getId());
        dto.setEntryType(entity.getEntryType());

        dto.setBenchNo(entity.getBenchNo());
        dto.setBenchFrom(entity.getBenchFrom());
        dto.setBenchTo(entity.getBenchTo());

        dto.setNoOfBenches(entity.getNoOfBenches());

        dto.setSleeperCategory(entity.getSleeperCategory());
        dto.setMouldsPerBench(entity.getMouldsPerBench());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());

        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());

        return dto;
    } */
}