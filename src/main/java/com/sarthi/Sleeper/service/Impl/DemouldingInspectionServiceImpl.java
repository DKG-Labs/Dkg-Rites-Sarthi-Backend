package com.sarthi.Sleeper.service.Impl;


import com.sarthi.Sleeper.dto.DemouldingDefectiveSleeperDTO;
import com.sarthi.Sleeper.dto.DemouldingInspectionRequestDTO;
import com.sarthi.Sleeper.dto.DemouldingInspectionResponseDTO;
import com.sarthi.Sleeper.entity.DemouldingDefectiveSleeper;
import com.sarthi.Sleeper.entity.DemouldingInspection;
import com.sarthi.Sleeper.repository.DemouldingDefectiveSleeperRepository;
import com.sarthi.Sleeper.repository.DemouldingInspectionRepository;
import com.sarthi.Sleeper.service.DemouldingInspectionService;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DemouldingInspectionServiceImpl implements DemouldingInspectionService {

    @Autowired
    private DemouldingInspectionRepository demouldingInspectionRepository;
    @Autowired
    private DemouldingDefectiveSleeperRepository demouldingDefectiveSleeperRepository;
    @Override
    public DemouldingInspectionResponseDTO create(
            DemouldingInspectionRequestDTO dto) {

        DemouldingInspection entity = new DemouldingInspection();

        entity.setLineShedNo(dto.getLineShedNo());

        LocalDate iDate = CommonUtils.convertStringToDateObject(dto.getInspectionDate());
        if (dto.getInspectionDate() != null) {
            entity.setInspectionDate(iDate);
        }

        entity.setInspectionTime(dto.getInspectionTime());

        LocalDate cDate = CommonUtils.convertStringToDateObject(dto.getCastingDate());

        if (dto.getCastingDate() != null) {
            entity.setCastingDate(cDate);
        }

        entity.setBatchNo(dto.getBatchNo());
        entity.setBenchNo(dto.getBenchNo());
        entity.setSleeperType(dto.getSleeperType());
        entity.setProcessStatus(dto.getProcessStatus());
        entity.setVisualCheck(dto.getVisualCheck());
        entity.setDimCheck(dto.getDimCheck());
        entity.setOverallRemarks(dto.getOverallRemarks());

        entity.setCreatedBy(dto.getCreatedBy());



        /* Clear old defects */
        if (entity.getDefectiveSleepers() != null) {
            entity.getDefectiveSleepers().clear();
        }

        /* Map defects */
        if (dto.getDefectiveSleepers() != null) {

            List<DemouldingDefectiveSleeper> list =
                    new ArrayList<>();

            for (DemouldingDefectiveSleeperDTO d
                    : dto.getDefectiveSleepers()) {

                DemouldingDefectiveSleeper defect =
                        new DemouldingDefectiveSleeper();

                defect.setBenchGangNo(d.getBenchGangNo());
                defect.setSequenceNo(d.getSequenceNo());
                defect.setSleeperNo(d.getSleeperNo());
                defect.setVisualReason(d.getVisualReason());
                defect.setDimReason(d.getDimReason());

                defect.setCreatedDate(LocalDateTime.now());
                defect.setInspection(entity);

                list.add(defect);
            }

            entity.setDefectiveSleepers(list);
        }

        entity.setCreatedDate(LocalDateTime.now());
        entity.setStatus("A");

        validateDefects(dto);

        DemouldingInspection saved =
                demouldingInspectionRepository.save(entity);

        return mapToResponse(saved);
    }

    @Override
    public DemouldingInspectionResponseDTO update(
            Long id,
            DemouldingInspectionRequestDTO dto) {

        DemouldingInspection entity =
                demouldingInspectionRepository.findById(id)
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Demoulding Inspection not found for the provided Id.")
                        ));
        entity.setLineShedNo(dto.getLineShedNo());

        LocalDate iDate = CommonUtils.convertStringToDateObject(dto.getInspectionDate());
        if (iDate!= null) {
            entity.setInspectionDate(iDate);
        }

        entity.setInspectionTime(dto.getInspectionTime());

        LocalDate cDate = CommonUtils.convertStringToDateObject(dto.getCastingDate());

        if (cDate != null) {
            entity.setCastingDate(cDate);
        }

        entity.setBatchNo(dto.getBatchNo());
        entity.setBenchNo(dto.getBenchNo());
        entity.setSleeperType(dto.getSleeperType());
        entity.setProcessStatus(dto.getProcessStatus());
        entity.setVisualCheck(dto.getVisualCheck());
        entity.setDimCheck(dto.getDimCheck());
        entity.setOverallRemarks(dto.getOverallRemarks());

        entity.setUpdatedBy(dto.getUpdatedBy());

        if (entity.getDefectiveSleepers() == null) {
            entity.setDefectiveSleepers(new ArrayList<>());
        }

// Clear old records
        entity.getDefectiveSleepers().clear();

// Add new ones
        if (dto.getDefectiveSleepers() != null) {

            for (DemouldingDefectiveSleeperDTO d : dto.getDefectiveSleepers()) {

                DemouldingDefectiveSleeper defect =
                        new DemouldingDefectiveSleeper();

                defect.setBenchGangNo(d.getBenchGangNo());
                defect.setSequenceNo(d.getSequenceNo());
                defect.setSleeperNo(d.getSleeperNo());
                defect.setVisualReason(d.getVisualReason());
                defect.setDimReason(d.getDimReason());

                defect.setCreatedDate(LocalDateTime.now());

                defect.setInspection(entity);

                entity.getDefectiveSleepers().add(defect);
            }
        }



        entity.setStatus("A");

        entity.setUpdatedDate(LocalDateTime.now());

        validateDefects(dto);

        DemouldingInspection updated =
                demouldingInspectionRepository.save(entity);

        return mapToResponse(updated);
    }


    @Override
    public DemouldingInspectionResponseDTO getById(Long id) {

        DemouldingInspection entity =
                demouldingInspectionRepository.findById(id)
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Demoulding Inspection not found for the provided Id.")
                        ));
        return mapToResponse(entity);
    }


    @Override
    public List<DemouldingInspectionResponseDTO> getAll() {

        return demouldingInspectionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    /* ================= DELETE ================= */

    @Override
    public void delete(Long id) {

        if (!demouldingInspectionRepository.existsById(id)) {
            throw new RuntimeException("Record not found");
        }

        demouldingInspectionRepository.deleteById(id);
    }


    /* ================= BUSINESS RULE ================= */

    private void validateDefects(
            DemouldingInspectionRequestDTO dto) {

        if ("ALL_OK".equalsIgnoreCase(dto.getDimCheck())) {

            // No defects allowed
            if (dto.getDefectiveSleepers() != null
                    && !dto.getDefectiveSleepers().isEmpty()) {

                throw new RuntimeException(
                        "No defective sleepers allowed when ALL_OK");
            }

        } else {

            // Must have defects
            if (dto.getDefectiveSleepers() == null
                    || dto.getDefectiveSleepers().isEmpty()) {

                throw new RuntimeException(
                        "Defective sleeper details required");
            }
        }
    }


    private DemouldingInspectionResponseDTO mapToResponse(
            DemouldingInspection entity) {

        DemouldingInspectionResponseDTO dto =
                new DemouldingInspectionResponseDTO();

        dto.setId(entity.getId());
        dto.setLineShedNo(entity.getLineShedNo());

        String iDate = CommonUtils.convertDateToString(entity.getInspectionDate());
        if (dto.getInspectionDate() != null) {
            dto.setInspectionDate(iDate);
        }

        entity.setInspectionTime(dto.getInspectionTime());

        String cDate = CommonUtils.convertDateToString(entity.getCastingDate());

        if (dto.getCastingDate() != null) {
            dto.setCastingDate(cDate);
        }

        dto.setBatchNo(entity.getBatchNo());
        dto.setBenchNo(entity.getBenchNo());
        dto.setSleeperType(entity.getSleeperType());
        dto.setProcessStatus(entity.getProcessStatus());
        dto.setVisualCheck(entity.getVisualCheck());
        dto.setDimCheck(entity.getDimCheck());
        dto.setOverallRemarks(entity.getOverallRemarks());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedDate(entity.getUpdatedDate());
        dto.setStatus(entity.getStatus());


        /* Map defects */
        if (entity.getDefectiveSleepers() != null) {

            List<DemouldingDefectiveSleeperDTO> list =
                    new ArrayList<>();

            for (DemouldingDefectiveSleeper d
                    : entity.getDefectiveSleepers()) {

                DemouldingDefectiveSleeperDTO dd =
                        new DemouldingDefectiveSleeperDTO();

                dd.setBenchGangNo(d.getBenchGangNo());
                dd.setSequenceNo(d.getSequenceNo());
                dd.setSleeperNo(d.getSleeperNo());
                dd.setVisualReason(d.getVisualReason());
                dd.setDimReason(d.getDimReason());

                list.add(dd);
            }

            dto.setDefectiveSleepers(list);
        }

        return dto;
    }
}
