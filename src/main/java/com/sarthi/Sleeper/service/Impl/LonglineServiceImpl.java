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

        // mapping
        entity.setLineFrom(dto.getLineFrom());
        entity.setLineTo(dto.getLineTo());
        entity.setNoOfLines(dto.getNoOfLines());
        entity.setMouldsPerLine(dto.getMouldsPerLine());
        entity.setSleeperCategory(dto.getSleeperCategory());
        entity.setEntryType(dto.getEntryType());

        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());

        repository.save(entity);

        return mapToResponse(entity);
    }

    @Override
    public LonglineResponseDTO update(Long id, LonglineRequestDTO dto) {

        LonglineMaster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        entity.setLineFrom(dto.getLineFrom());
        entity.setLineTo(dto.getLineTo());
        entity.setNoOfLines(dto.getNoOfLines());
        entity.setMouldsPerLine(dto.getMouldsPerLine());
        entity.setSleeperCategory(dto.getSleeperCategory());
        entity.setEntryType(dto.getEntryType());

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
        dto.setLineFrom(entity.getLineFrom());
        dto.setLineTo(entity.getLineTo());
        dto.setNoOfLines(entity.getNoOfLines());
        dto.setMouldsPerLine(entity.getMouldsPerLine());
        dto.setSleeperCategory(entity.getSleeperCategory());
        dto.setEntryType(entity.getEntryType());

        String status = sleeperWorkflowRepository
                .findLatestStatusByRequestIdAndModuleId(String.valueOf(entity.getId()), 12L)
                .orElse("NOT_STARTED");
        if (status != null) {
            dto.setStatus(status);
        }
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedDate(entity.getUpdatedDate());

        return dto;
    }
}
