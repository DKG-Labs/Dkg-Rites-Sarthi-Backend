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
    public synchronized RailRheometerTestDto createRheometerTest(RailRheometerTestDto dto) {
        rejectDuplicate(dto, null);

        RailRheometerTest entity = new RailRheometerTest();
        BeanUtils.copyProperties(dto, entity);
        entity = repository.save(entity);

        RailRheometerTestDto savedDto = new RailRheometerTestDto();
        BeanUtils.copyProperties(entity, savedDto);
        return savedDto;
    }

    @Override
    public synchronized RailRheometerTestDto updateRheometerTest(Long id, RailRheometerTestDto dto) {
        RailRheometerTest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found with ID: " + id));
        // an edit changes the values, never which day / shift / plant the check belongs to
        dto.setCastingDate(entity.getCastingDate());
        dto.setShift(entity.getShift());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());
        rejectDuplicate(dto, id);

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
        return getRheometerTestByPlantAndVendor(plantId, vendorCode, null, null);
    }

    @Override
    public List<RailRheometerTestDto> getRheometerTestByPlantAndVendor(String plantId, String vendorCode, String fromDate, String toDate) {
        String cleanPlantId = (plantId != null && plantId.startsWith(":")) ? plantId.substring(1) : plantId;
        String colonPlantId = (plantId != null && !plantId.startsWith(":")) ? ":" + plantId : plantId;

        List<RailRheometerTest> list = repository.findByPlantIdAndVendorCodeOrderByCreatedDateDesc(plantId, vendorCode);
        if ((list == null || list.isEmpty()) && cleanPlantId != null && !cleanPlantId.equals(plantId)) {
            list = repository.findByPlantIdAndVendorCodeOrderByCreatedDateDesc(cleanPlantId, vendorCode);
        } else if ((list == null || list.isEmpty()) && colonPlantId != null && !colonPlantId.equals(plantId)) {
            list = repository.findByPlantIdAndVendorCodeOrderByCreatedDateDesc(colonPlantId, vendorCode);
        }

        if (list == null) return java.util.Collections.emptyList();

        return list.stream()
                .filter(entity -> {
                    String d = entity.getCreatedDate() != null ? entity.getCreatedDate().toLocalDate().toString() : null;
                    if (d == null) return true;
                    if (fromDate != null && !fromDate.isBlank() && d.compareTo(fromDate) < 0) return false;
                    if (toDate != null && !toDate.isBlank() && d.compareTo(toDate) > 0) return false;
                    return true;
                })
                .map(entity -> {
                    RailRheometerTestDto dto = new RailRheometerTestDto();
                    BeanUtils.copyProperties(entity, dto);
                    return dto;
                }).collect(Collectors.toList());
    }

    /**
     * Same batch number at the plant is a duplicate only within the same date and shift. Plants restart batch numbers,
     * so a recycled number on another day is a new check. Rows without date / shift never block.
     */
    private void rejectDuplicate(RailRheometerTestDto dto, Long excludeId) {
        String batch = dto.getBatchNo() == null ? "" : dto.getBatchNo().trim();
        if (batch.isEmpty()) throw new RuntimeException("Batch Number is required.");
        dto.setBatchNo(batch);
        String date = dto.getCastingDate() == null ? "" : dto.getCastingDate().trim();
        String shift = dto.getShift() == null ? "" : dto.getShift().trim();
        if (date.isEmpty()) return;   // no date sent: no duplicate rule
        String plant = dto.getPlantId() == null ? "" : dto.getPlantId().trim();
        String bare = plant.startsWith(":") ? plant.substring(1) : plant;
        if (!repository.findSameBatchDateShift(java.util.List.of(bare, ":" + bare), dto.getVendorCode(), batch.toUpperCase(),
                date, shift.toUpperCase(), excludeId).isEmpty()) {
            throw new RuntimeException("Batch " + batch + " is already entered for " + date
                    + (shift.isEmpty() ? "" : ", Shift " + shift) + ". Edit that entry instead of adding it again.");
        }
    }
}
