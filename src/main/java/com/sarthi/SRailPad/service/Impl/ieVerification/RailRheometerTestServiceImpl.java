package com.sarthi.SRailPad.service.Impl.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailRheometerTestDto;
import com.sarthi.SRailPad.entity.ieVerification.RailRheometerTest;
import com.sarthi.SRailPad.repository.ieVerification.RailRheometerTestRepository;
import com.sarthi.SRailPad.service.ieVerification.RailRheometerTestService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RailRheometerTestServiceImpl implements RailRheometerTestService {

    @Autowired
    private RailRheometerTestRepository repository;

    @Override
    public RailRheometerTestDto createRheometerTest(RailRheometerTestDto dto) {
        Optional<RailRheometerTest> existing = repository.findByBatchNoAndPlantIdAndVendorCode(dto.getBatchNo(), dto.getPlantId(), dto.getVendorCode());
        if (existing.isPresent()) {
            throw new RuntimeException("A record with this Batch Number already exists for the given Plant and Vendor.");
        }

        RailRheometerTest entity = new RailRheometerTest();
        BeanUtils.copyProperties(dto, entity);
        entity = repository.save(entity);

        RailRheometerTestDto savedDto = new RailRheometerTestDto();
        BeanUtils.copyProperties(entity, savedDto);
        return savedDto;
    }

    @Override
    public RailRheometerTestDto updateRheometerTest(Long id, RailRheometerTestDto dto) {
        RailRheometerTest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found with ID: " + id));

        Optional<RailRheometerTest> existing = repository.findByBatchNoAndPlantIdAndVendorCode(dto.getBatchNo(), dto.getPlantId(), dto.getVendorCode());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new RuntimeException("A record with this Batch Number already exists for the given Plant and Vendor.");
        }

        entity.setBatchNo(dto.getBatchNo());
        entity.setVulcanTime(dto.getVulcanTime());
        entity.setVulcanTemp(dto.getVulcanTemp());
        entity.setEnsured(dto.getEnsured());
        entity.setStatus(dto.getStatus());
        entity.setUpdatedBy(dto.getUpdatedBy());

        entity = repository.save(entity);

        RailRheometerTestDto updatedDto = new RailRheometerTestDto();
        BeanUtils.copyProperties(entity, updatedDto);
        return updatedDto;
    }

    @Override
    public void deleteRheometerTest(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Record not found with ID: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public RailRheometerTestDto getRheometerTestById(Long id) {
        RailRheometerTest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found with ID: " + id));
        RailRheometerTestDto dto = new RailRheometerTestDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    public List<RailRheometerTestDto> getRheometerTestByPlantAndVendor(String plantId, String vendorCode) {
        List<RailRheometerTest> list = repository.findByPlantIdAndVendorCodeOrderByCreatedDateDesc(plantId, vendorCode);
        return list.stream().map(entity -> {
            RailRheometerTestDto dto = new RailRheometerTestDto();
            BeanUtils.copyProperties(entity, dto);
            return dto;
        }).collect(Collectors.toList());
    }
}
