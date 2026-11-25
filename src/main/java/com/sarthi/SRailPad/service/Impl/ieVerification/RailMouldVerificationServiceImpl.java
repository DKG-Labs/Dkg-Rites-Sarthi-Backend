package com.sarthi.SRailPad.service.Impl.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailMouldVerificationDto;
import com.sarthi.SRailPad.entity.ieVerification.RailMouldVerification;
import com.sarthi.SRailPad.repository.ieVerification.RailMouldVerificationRepository;
import com.sarthi.SRailPad.service.ieVerification.RailMouldVerificationService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailMouldVerificationServiceImpl implements RailMouldVerificationService {

    @Autowired
    private RailMouldVerificationRepository repository;

    /**
     * A mould is verified once per date + shift. The same mould on another date or shift is a new check (allowed);
     * the same mould again in the same date + shift is a duplicate (rejected; edit the existing entry instead).
     */
    private void rejectDuplicate(RailMouldVerificationDto dto, Long excludeId) {
        String mould = dto.getMouldNumber() == null ? "" : dto.getMouldNumber().trim();
        if (mould.isEmpty()) throw new RuntimeException("Mould / Drawing Number is required.");
        dto.setMouldNumber(mould);
        String plant = dto.getPlantId() == null ? "" : dto.getPlantId().trim();
        String bare = plant.startsWith(":") ? plant.substring(1) : plant;
        List<String> plantIds = List.of(bare, ":" + bare);
        String date = dto.getCastingDate() == null ? "" : dto.getCastingDate().trim();
        String shift = dto.getShift() == null ? "" : dto.getShift().trim().toUpperCase();

        if (!repository.findSameMouldDateShift(plantIds, dto.getVendorCode(), mould.toUpperCase(), date, shift, excludeId).isEmpty()) {
            throw new RuntimeException("Mould " + mould + " is already verified for " + (date.isEmpty() ? "this date" : date)
                    + (shift.isEmpty() ? "" : ", Shift " + dto.getShift().trim())
                    + ". Edit that entry instead of adding it again.");
        }
    }

    /** synchronized: the duplicate check and the insert (auto-committed by save) run as one step per server. */
    @Override
    public synchronized RailMouldVerificationDto createMouldVerification(RailMouldVerificationDto dto) {
        try {
            rejectDuplicate(dto, null);

            RailMouldVerification entity = new RailMouldVerification();
            BeanUtils.copyProperties(dto, entity);
            entity = repository.save(entity);

            RailMouldVerificationDto savedDto = new RailMouldVerificationDto();
            BeanUtils.copyProperties(entity, savedDto);
            return savedDto;
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw e;
            }
            throw new RuntimeException("An error occurred while creating the Mould Verification record.");
        }
    }

    @Override
    public synchronized RailMouldVerificationDto updateMouldVerification(Long id, RailMouldVerificationDto dto) {
        try {
            RailMouldVerification entity = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Mould Verification record not found with id: " + id));
            // An edit changes the check's values, never which day / shift / plant it belongs to
            // (the form sends the current shift's date, which would move an older entry to today).
            dto.setCastingDate(entity.getCastingDate());
            dto.setShift(entity.getShift());
            dto.setPlantId(entity.getPlantId());
            dto.setVendorCode(entity.getVendorCode());
            rejectDuplicate(dto, id);

            BeanUtils.copyProperties(dto, entity, "id", "createdDate", "createdBy");
            entity = repository.save(entity);

            RailMouldVerificationDto updatedDto = new RailMouldVerificationDto();
            BeanUtils.copyProperties(entity, updatedDto);
            return updatedDto;
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw e;
            }
            throw new RuntimeException("An error occurred while updating the Mould Verification record.");
        }
    }

    @Override
    public void deleteMouldVerification(Long id) {
        try {
            if (!repository.existsById(id)) {
                throw new RuntimeException("Mould Verification record not found with id: " + id);
            }
            repository.deleteById(id);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw e;
            }
            throw new RuntimeException("An error occurred while deleting the Mould Verification record.");
        }
    }

    @Override
    public List<RailMouldVerificationDto> getMouldVerifications(String plantId, String vendorCode) {
        return getMouldVerifications(plantId, vendorCode, null, null);
    }

    @Override
    public List<RailMouldVerificationDto> getMouldVerifications(String plantId, String vendorCode, String fromDate, String toDate) {
        try {
            String cleanPlantId = (plantId != null && plantId.startsWith(":")) ? plantId.substring(1) : plantId;
            String colonPlantId = (plantId != null && !plantId.startsWith(":")) ? ":" + plantId : plantId;

            List<RailMouldVerification> entities = repository.findByPlantIdAndVendorCodeOrderByCreatedDateDesc(plantId, vendorCode);
            if ((entities == null || entities.isEmpty()) && cleanPlantId != null && !cleanPlantId.equals(plantId)) {
                entities = repository.findByPlantIdAndVendorCodeOrderByCreatedDateDesc(cleanPlantId, vendorCode);
            } else if ((entities == null || entities.isEmpty()) && colonPlantId != null && !colonPlantId.equals(plantId)) {
                entities = repository.findByPlantIdAndVendorCodeOrderByCreatedDateDesc(colonPlantId, vendorCode);
            }

            if (entities == null) return java.util.Collections.emptyList();

            return entities.stream()
                .filter(entity -> {
                    String d = entity.getCastingDate();
                    if ((d == null || d.isBlank()) && entity.getCreatedDate() != null) {
                        d = entity.getCreatedDate().toLocalDate().toString();
                    }
                    if (d == null) return true;
                    if (fromDate != null && !fromDate.isBlank() && d.compareTo(fromDate) < 0) return false;
                    if (toDate != null && !toDate.isBlank() && d.compareTo(toDate) > 0) return false;
                    return true;
                })
                .map(entity -> {
                    RailMouldVerificationDto dto = new RailMouldVerificationDto();
                    BeanUtils.copyProperties(entity, dto);
                    return dto;
                }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching Mould Verification records.");
        }
    }
}
