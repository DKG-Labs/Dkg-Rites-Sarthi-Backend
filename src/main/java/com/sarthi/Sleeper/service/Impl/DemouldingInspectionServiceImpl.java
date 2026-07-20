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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
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

        entity.setVendorCode(dto.getVendorCode());
        entity.setPlantId(dto.getPlantId());
        entity.setShift(dto.getShift());


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


        entity.setVendorCode(dto.getVendorCode());
        entity.setPlantId(dto.getPlantId());
        entity.setShift(dto.getShift());

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

    private void validateDefects(DemouldingInspectionRequestDTO dto) {

        String visual = dto.getVisualCheck();
        String dim = dto.getDimCheck();

        List<DemouldingDefectiveSleeperDTO> defects = dto.getDefectiveSleepers();

        boolean visualOk = "ALL_OK".equalsIgnoreCase(visual);
        boolean dimOk = "ALL_OK".equalsIgnoreCase(dim);

        //  Case 1: Both ALL_OK → no defects
        if (visualOk && dimOk) {
            if (defects != null && !defects.isEmpty()) {
                throw new RuntimeException("No defects allowed when both are ALL_OK");
            }
            return;
        }

        //  If any one is not OK → defects required
        if (defects == null || defects.isEmpty()) {
            throw new RuntimeException("Defective sleeper details required");
        }

        //  Validate each sleeper
        for (DemouldingDefectiveSleeperDTO d : defects) {

            // Only DIM issue
            if (visualOk && !dimOk) {

                if (d.getDimReason() == null) {
                    throw new RuntimeException(
                            "Dim reason required for sleeper: " + d.getSleeperNo());
                }

                // Optional cleanup
                d.setVisualReason(null);
            }

            // Only VISUAL issue
            else if (!visualOk && dimOk) {

                if (d.getVisualReason() == null) {
                    throw new RuntimeException(
                            "Visual reason required for sleeper: " + d.getSleeperNo());
                }

                d.setDimReason(null);
            }

            // BOTH issues
            else {

                if (d.getVisualReason() == null || d.getDimReason() == null) {
                    throw new RuntimeException(
                            "Both reasons required for sleeper: " + d.getSleeperNo());
                }
            }
        }
    }


    private DemouldingInspectionResponseDTO mapToResponse(
            DemouldingInspection entity) {

        DemouldingInspectionResponseDTO dto =
                new DemouldingInspectionResponseDTO();

        dto.setId(entity.getId());
        dto.setLineShedNo(entity.getLineShedNo());

        if (entity.getInspectionDate() != null) {
            dto.setInspectionDate(
                    CommonUtils.convertDateToString(entity.getInspectionDate()));
        }

        dto.setInspectionTime(entity.getInspectionTime());

        if (entity.getCastingDate() != null) {
            dto.setCastingDate(
                    CommonUtils.convertDateToString(entity.getCastingDate()));
        }

        dto.setBatchNo(entity.getBatchNo());
        dto.setBenchNo(entity.getBenchNo());
        dto.setSleeperType(entity.getSleeperType());
        dto.setProcessStatus(entity.getProcessStatus());
        dto.setVisualCheck(entity.getVisualCheck());
        dto.setDimCheck(entity.getDimCheck());
        dto.setOverallRemarks(entity.getOverallRemarks());

        dto.setVendorCode(entity.getVendorCode());
        dto.setPlantId(entity.getPlantId());
        dto.setShift(entity.getShift());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedDate(entity.getUpdatedDate());
        dto.setStatus(entity.getStatus());

        if (entity.getDefectiveSleepers() != null) {

            List<DemouldingDefectiveSleeperDTO> list = new ArrayList<>();

            for (DemouldingDefectiveSleeper d : entity.getDefectiveSleepers()) {

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

    /*

    @Override
    public List<DemouldingInspectionResponseDTO> getTodayRecords(
            String plantId,
            String vendorCode,
            String shift,
            String createdBy,
            String date) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate selectedDate = LocalDate.parse(date, formatter);

        LocalDateTime startOfDay = selectedDate.atStartOfDay();
        LocalDateTime endOfDay = selectedDate.atTime(23, 59, 59);

        List<DemouldingInspection> list = demouldingInspectionRepository.findByDate(
                plantId,
                vendorCode,
                shift,
                createdBy,
                startOfDay,
                endOfDay
        );

        return list.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


     */

    @Override
    public List<DemouldingInspectionResponseDTO> getTodayRecords(
            String plantId,
            String vendorCode,
            String shift,
            String createdBy,
            String date) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate selectedDate = LocalDate.parse(date, formatter);

        LocalDateTime startOfDay;
        LocalDateTime endOfDay;

        if ("C".equalsIgnoreCase(shift)) {
            // Shift C IST: 10 PM → 6 AM next day
            // DB stores LocalDateTime.now() in UTC (serverTimezone=UTC)
            // IST 22:00 = UTC 16:30 | IST 06:00 next day = UTC 00:30 next day
            startOfDay = selectedDate.atTime(16, 30, 0);
            endOfDay = selectedDate.plusDays(1).atTime(0, 30, 0);
        } else {
            // Normal shifts A & B
            startOfDay = selectedDate.atStartOfDay();
            endOfDay = selectedDate.atTime(23, 59, 59);
        }

        List<DemouldingInspection> list = demouldingInspectionRepository.findByDate(
                plantId,
                vendorCode,
                shift,
                createdBy,
                startOfDay,
                endOfDay
        );

        return list.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}
