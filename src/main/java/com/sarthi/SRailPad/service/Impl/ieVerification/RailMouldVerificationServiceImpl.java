package com.sarthi.SRailPad.service.Impl.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailMouldVerificationDto;
import com.sarthi.SRailPad.entity.ieVerification.RailMouldVerification;
import com.sarthi.SRailPad.repository.ieVerification.RailMouldVerificationRepository;
import com.sarthi.SRailPad.service.ieVerification.RailMouldVerificationService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RailMouldVerificationServiceImpl implements RailMouldVerificationService {

    @Autowired
    private RailMouldVerificationRepository repository;

    @Override
    public RailMouldVerificationDto createMouldVerification(RailMouldVerificationDto dto) {
        try {
            Optional<RailMouldVerification> existing = repository.findByMouldNumberAndPlantIdAndVendorCode(dto.getMouldNumber(), dto.getPlantId(), dto.getVendorCode());
            if (existing.isPresent()) {
                throw new RuntimeException("A record with this Mould Number already exists for the given Plant and Vendor.");
            }

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
    public RailMouldVerificationDto updateMouldVerification(Long id, RailMouldVerificationDto dto) {
        try {
            RailMouldVerification entity = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Mould Verification record not found with id: " + id));

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
        try {
            List<RailMouldVerification> entities = repository.findByPlantIdAndVendorCodeOrderByCreatedDateDesc(plantId, vendorCode);
            return entities.stream().map(entity -> {
                RailMouldVerificationDto dto = new RailMouldVerificationDto();
                BeanUtils.copyProperties(entity, dto);
                return dto;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching Mould Verification records.");
        }
    }
}
