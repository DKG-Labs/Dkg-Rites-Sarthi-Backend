package com.sarthi.service.Impl;

import com.sarthi.dto.processmaterial.ProcessStaticPeriodicCheckDTO;
import com.sarthi.entity.processmaterial.ProcessStaticPeriodicCheck;
import com.sarthi.repository.processmaterial.ProcessStaticPeriodicCheckRepository;
import com.sarthi.service.ProcessStaticPeriodicCheckService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProcessStaticPeriodicCheckServiceImpl implements ProcessStaticPeriodicCheckService {

    @Autowired
    private ProcessStaticPeriodicCheckRepository repository;

    @Override
    public List<ProcessStaticPeriodicCheckDTO> getByInspectionCallNo(String inspectionCallNo) {
        return repository.findByInspectionCallNo(inspectionCallNo)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ProcessStaticPeriodicCheckDTO> getByCallNoPoNoLineNo(
            String inspectionCallNo, String poNo, String lineNo) {
        return repository.findByInspectionCallNoAndPoNoAndLineNo(inspectionCallNo, poNo, lineNo)
                .map(this::toDTO);
    }

    @Override
    @Transactional
    public ProcessStaticPeriodicCheckDTO save(ProcessStaticPeriodicCheckDTO dto) {
        Optional<ProcessStaticPeriodicCheck> existing = repository.findByInspectionCallNoAndShiftAndLineNoAndLotNoAndCreatedByAndDateOfInspection(
                dto.getInspectionCallNo(), dto.getShift(), dto.getLineNo(), dto.getLotNo(), dto.getCreatedBy(), dto.getDateOfInspection());
        
        ProcessStaticPeriodicCheck entity;
        if (existing.isPresent()) {
            entity = existing.get();
            // Update fields manually or with BeanUtils excluding ID/CreatedAt
            Long id = entity.getId();
            java.time.LocalDateTime createdAt = entity.getCreatedAt();
            BeanUtils.copyProperties(dto, entity);
            entity.setId(id);
            entity.setCreatedAt(createdAt);
        } else {
            entity = toEntity(dto);
        }
        
        ProcessStaticPeriodicCheck saved = repository.save(entity);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public void deleteByInspectionCallNo(String inspectionCallNo) {
        repository.deleteByInspectionCallNo(inspectionCallNo);
    }

    private ProcessStaticPeriodicCheckDTO toDTO(ProcessStaticPeriodicCheck entity) {
        ProcessStaticPeriodicCheckDTO dto = new ProcessStaticPeriodicCheckDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private ProcessStaticPeriodicCheck toEntity(ProcessStaticPeriodicCheckDTO dto) {
        ProcessStaticPeriodicCheck entity = new ProcessStaticPeriodicCheck();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}

