package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.repository.SleeperWorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sarthi.Sleeper.dto.*;
import com.sarthi.Sleeper.entity.LonglineMaster;
import com.sarthi.Sleeper.repository.LonglineRepository;
import com.sarthi.Sleeper.service.LonglineService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LonglineServiceImpl implements LonglineService {

    private final LonglineRepository repository;

    @Autowired
    private SleeperWorkflowRepository sleeperWorkflowRepository;

    @Override
    public LonglineResponseDTO create(LonglineRequestDTO dto) {

        LonglineMaster entity = new LonglineMaster();


        entity.setCategory(dto.getCategory());
        entity.setMouldsPerGang(dto.getMouldsPerGang());
        entity.setEntryMode(dto.getEntryMode());


        if ("RANGE".equalsIgnoreCase(dto.getEntryMode())) {

            entity.setGangFrom(dto.getGangFrom());
            entity.setGangTo(dto.getGangTo());
            entity.setGangNo(null);

            // auto count (recommended)
            if (dto.getGangFrom() != null && dto.getGangTo() != null) {
                entity.setCount(dto.getGangTo() - dto.getGangFrom() + 1);
            }

        } else {

            entity.setGangNo(dto.getGangNo());
            entity.setGangFrom(null);
            entity.setGangTo(null);
            entity.setCount(1);
        }

        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());

        repository.save(entity);

        return mapToResponse(entity);
    }

    @Override
    public LonglineResponseDTO update(Long id, LonglineRequestDTO dto) {

        LonglineMaster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));


        entity.setCategory(dto.getCategory());
        entity.setMouldsPerGang(dto.getMouldsPerGang());
        entity.setEntryMode(dto.getEntryMode());


        if ("RANGE".equalsIgnoreCase(dto.getEntryMode())) {

            entity.setGangFrom(dto.getGangFrom());
            entity.setGangTo(dto.getGangTo());
            entity.setGangNo(null);

            if (dto.getGangFrom() != null && dto.getGangTo() != null) {
                entity.setCount(dto.getGangTo() - dto.getGangFrom() + 1);
            }

        } else {

            entity.setGangNo(dto.getGangNo());
            entity.setGangFrom(null);
            entity.setGangTo(null);
            entity.setCount(1);
        }

        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        repository.save(entity);

        return mapToResponse(entity);
    }

    @Override
    public LonglineResponseDTO getById(Long id) {

        LonglineMaster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        return mapToResponse(entity);
    }

    @Override
    public List<LonglineResponseDTO> getAll() {

        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }


    private LonglineResponseDTO mapToResponse(LonglineMaster entity) {

        LonglineResponseDTO dto = new LonglineResponseDTO();

        dto.setId(entity.getId());

        dto.setGangFrom(entity.getGangFrom());
        dto.setGangTo(entity.getGangTo());
        dto.setGangNo(entity.getGangNo());
        dto.setCount(entity.getCount());
        dto.setMouldsPerGang(entity.getMouldsPerGang());
        dto.setCategory(entity.getCategory());
        dto.setEntryMode(entity.getEntryMode());

        String status = sleeperWorkflowRepository
                .findLatestStatusByRequestIdAndModuleId(String.valueOf(entity.getId()), 12L)
                .orElse("NOT_STARTED");

        dto.setStatus(status);

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedDate(entity.getUpdatedDate());

        return dto;
    }
}