package com.sarthi.SRailPad.service.Impl.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailSheetingSizingDto;
import com.sarthi.SRailPad.entity.ieVerification.RailSheetingSizing;
import com.sarthi.SRailPad.repository.ieVerification.RailSheetingSizingRepository;
import com.sarthi.SRailPad.service.ieVerification.RailSheetingSizingService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RailSheetingSizingServiceImpl implements RailSheetingSizingService {

    @Autowired
    private RailSheetingSizingRepository repository;

    @Override
    public RailSheetingSizingDto createSheetingSizing(RailSheetingSizingDto dto) {
        // Validation for existing batchNo within same plant and vendor
        Optional<RailSheetingSizing> existing = repository.findByBatchNoAndPlantIdAndVendorCode(dto.getBatchNo(), dto.getPlantId(), dto.getVendorCode());
        if (existing.isPresent()) {
            throw new RuntimeException("A record with this Batch Number already exists for the given Plant and Vendor.");
        }

        RailSheetingSizing entity = new RailSheetingSizing();
        BeanUtils.copyProperties(dto, entity);
        entity = repository.save(entity);
        
        RailSheetingSizingDto savedDto = new RailSheetingSizingDto();
        BeanUtils.copyProperties(entity, savedDto);
        return savedDto;
    }

    @Override
    public RailSheetingSizingDto updateSheetingSizing(Long id, RailSheetingSizingDto dto) {
        RailSheetingSizing entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found with ID: " + id));

        // Check if updating to a batch no that exists elsewhere
        Optional<RailSheetingSizing> existing = repository.findByBatchNoAndPlantIdAndVendorCode(dto.getBatchNo(), dto.getPlantId(), dto.getVendorCode());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new RuntimeException("A record with this Batch Number already exists for the given Plant and Vendor.");
        }

        entity.setBatchNo(dto.getBatchNo());
        entity.setSheeting(dto.getSheeting());
        entity.setRemarks(dto.getRemarks());
        entity.setStatus(dto.getStatus());
        entity.setUpdatedBy(dto.getUpdatedBy());

        entity = repository.save(entity);

        RailSheetingSizingDto updatedDto = new RailSheetingSizingDto();
        BeanUtils.copyProperties(entity, updatedDto);
        return updatedDto;
    }

    @Override
    public void deleteSheetingSizing(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Record not found with ID: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public RailSheetingSizingDto getSheetingSizingById(Long id) {
        RailSheetingSizing entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found with ID: " + id));
        RailSheetingSizingDto dto = new RailSheetingSizingDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    public List<RailSheetingSizingDto> getSheetingSizingByPlantAndVendor(String plantId, String vendorCode) {
        List<RailSheetingSizing> list = repository.findByPlantIdAndVendorCodeOrderByCreatedDateDesc(plantId, vendorCode);
        return list.stream().map(entity -> {
            RailSheetingSizingDto dto = new RailSheetingSizingDto();
            BeanUtils.copyProperties(entity, dto);
            return dto;
        }).collect(Collectors.toList());
    }
}
