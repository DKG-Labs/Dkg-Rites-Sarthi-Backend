package com.sarthi.service.Impl;

import com.sarthi.constant.AppConstant;
import com.sarthi.dto.Calibration.CreateIeVendorCalibrationInspectionRequestDto;
import com.sarthi.dto.Calibration.IeVendorCalibrationInspectionDetailResponseDto;
import com.sarthi.dto.Calibration.IeVendorCalibrationInspectionResponseDto;
import com.sarthi.dto.VendorCalibrationDetailDto;
import com.sarthi.dto.VendorCalibrationHeaderRequestDto;
import com.sarthi.dto.VendorCalibrationHeaderResponseDto;
import com.sarthi.entity.IeVendorCalibrationInspection;
import com.sarthi.entity.IeVendorCalibrationInspectionDetail;
import com.sarthi.entity.VendorCalibrationDetail;
import com.sarthi.entity.VendorCalibrationHeader;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.repository.IeVendorCalibrationInspectionRepository;
import com.sarthi.repository.VendorCalibrationDetailRepository;
import com.sarthi.repository.VendorCalibrationHeaderRepository;
import com.sarthi.service.VendorCalibrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class VendorCalibrationServiceImpl implements VendorCalibrationService {

    private static final Logger logger = LoggerFactory.getLogger(VendorCalibrationServiceImpl.class);

    @Autowired
    private VendorCalibrationHeaderRepository headerRepository;

    @Autowired
    private VendorCalibrationDetailRepository detailRepository;

    @Autowired
    private IeVendorCalibrationInspectionRepository inspectionRepository;

    @Override
    public VendorCalibrationHeaderResponseDto createOrUpdateCalibrationGroup(VendorCalibrationHeaderRequestDto requestDto, String userId) {
        logger.info("Saving/updating calibration group for vendor: {}, category: {}", requestDto.getVendorCode(), requestDto.getCategory());

        try {
            // Validate basic inputs
            if (requestDto.getVendorCode() == null || requestDto.getVendorCode().trim().isEmpty()) {
                throw new IllegalArgumentException("Vendor code is required");
            }

            if (requestDto.getCategory() == null || requestDto.getCategory().trim().isEmpty()) {
                throw new IllegalArgumentException("Category is required");
            }

            // Save Base64 file if present
            String savedFilePath = requestDto.getCertificateFilePath();
            if (requestDto.getCertificateFileBase64() != null && !requestDto.getCertificateFileBase64().isEmpty()) {
                savedFilePath = requestDto.getCertificateFileBase64();
                logger.info("Calibration certificate file stored in DB as Base64 string");
            }

            VendorCalibrationHeader header;
            boolean isNew = true;

            // Check if we are updating an existing group by ID or matching combination
            if (requestDto.getId() != null) {
                header = headerRepository.findById(requestDto.getId())
                        .orElseThrow(() -> new BusinessException(new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_RESOURCE,
                                "Calibration group not found with ID: " + requestDto.getId())));
                isNew = false;
            } else {
                // Check if vendor + category combo already exists
                Optional<VendorCalibrationHeader> existing = headerRepository.findByVendorCodeAndCategory(
                        requestDto.getVendorCode(), requestDto.getCategory());
                if (existing.isPresent()) {
                    header = existing.get();
                    isNew = false;
                } else {
                    header = new VendorCalibrationHeader();
                    header.setVendorCode(requestDto.getVendorCode());
                    header.setCategory(requestDto.getCategory());
                }
            }

            // Set/update parent fields
            header.setCertificateFilePath(savedFilePath);
            if (isNew) {
                header.setCreatedBy(userId);
            }
            header.setUpdatedBy(userId);

            // Handle details. We'll clear the old ones if updating to support orphan removal.
            if (!isNew) {
                // Clear existing child records
                header.getDetails().clear();
                // Flush changes to detailRepository to ensure orphans are deleted first before adding new ones
                headerRepository.saveAndFlush(header);
            }

            // Add new child records
            if (requestDto.getDetails() != null) {
                for (VendorCalibrationDetailDto detailDto : requestDto.getDetails()) {
                    VendorCalibrationDetail detail = new VendorCalibrationDetail();
                    detail.setInstrumentName(detailDto.getInstrumentName());
                    detail.setCapacity(detailDto.getCapacity());
                    detail.setDescription(detailDto.getDescription());
                    detail.setUsedFor(detailDto.getUsedFor());
                    detail.setSerialNumber(detailDto.getSerialNumber());
                    detail.setCalibrationCertificateNo(detailDto.getCalibrationCertificateNo());
                    detail.setCalibrationDate(detailDto.getCalibrationDate());
                    detail.setCalibrationDueDate(detailDto.getCalibrationDueDate());
                    detail.setCertifyingLabName(detailDto.getCertifyingLabName());
                    detail.setAccreditationAgency(detailDto.getAccreditationAgency());
                    detail.setNotificationDays(detailDto.getNotificationDays() != null ? detailDto.getNotificationDays() : 30);
                    
                    // Auto calculate calibration status
                    String status = "Valid";
                    if (detailDto.getCalibrationDueDate() != null) {
                        if (detailDto.getCalibrationDueDate().isBefore(LocalDate.now())) {
                            status = "Expired";
                        }
                    }
                    detail.setCalibrationStatus(status);
                    
                    if (isNew) {
                        detail.setCreatedBy(userId);
                    } else {
                        detail.setCreatedBy(userId); // Or preserve original creator
                    }
                    detail.setUpdatedBy(userId);

                    header.addDetail(detail);
                }
            }

            VendorCalibrationHeader savedHeader = headerRepository.save(header);
            logger.info("Saved vendor calibration group successfully with ID: {}", savedHeader.getId());
            return mapToResponseDto(savedHeader);

        } catch (Exception e) {
            logger.error("Error saving vendor calibration: {}", e.getMessage(), e);
            throw new BusinessException(new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_INTERNAL,
                    AppConstant.ERROR_TYPE_ERROR,
                    "Failed to save vendor calibration: " + e.getMessage()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorCalibrationHeaderResponseDto> getCalibrationsByVendor(String vendorCode) {
        logger.info("Fetching calibration records for vendor: {}", vendorCode);
        try {
            List<VendorCalibrationHeader> headers = headerRepository.findByVendorCode(vendorCode);
            return headers.stream().map(this::mapToResponseDto).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching calibration records: {}", e.getMessage(), e);
            throw new BusinessException(new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_INTERNAL,
                    AppConstant.ERROR_TYPE_ERROR,
                    "Failed to fetch calibration records: " + e.getMessage()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public VendorCalibrationHeaderResponseDto getCalibrationGroupById(Long id) {
        logger.info("Fetching calibration record by ID: {}", id);
        VendorCalibrationHeader header = headerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(new ErrorDetails(
                        AppConstant.ERROR_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_RESOURCE,
                        "Calibration group not found with ID: " + id)));
        return mapToResponseDto(header);
    }

    @Override
    public void deleteCalibrationGroup(Long id) {
        logger.info("Deleting calibration group by ID: {}", id);
        VendorCalibrationHeader header = headerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(new ErrorDetails(
                        AppConstant.ERROR_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_RESOURCE,
                        "Calibration group not found with ID: " + id)));
        headerRepository.delete(header);
    }

    @Override
    public void deleteCalibrationDetail(Long detailId) {
        logger.info("Deleting individual calibration detail by ID: {}", detailId);
        VendorCalibrationDetail detail = detailRepository.findById(detailId)
                .orElseThrow(() -> new BusinessException(new ErrorDetails(
                        AppConstant.ERROR_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_RESOURCE,
                        "Calibration detail record not found with ID: " + detailId)));
        detailRepository.delete(detail);
    }

    private VendorCalibrationHeaderResponseDto mapToResponseDto(VendorCalibrationHeader header) {
        VendorCalibrationHeaderResponseDto dto = new VendorCalibrationHeaderResponseDto();
        dto.setId(header.getId());
        dto.setVendorCode(header.getVendorCode());

        dto.setCategory(header.getCategory());
        dto.setCertificateFilePath(header.getCertificateFilePath());
        dto.setCreatedBy(header.getCreatedBy());
        dto.setCreatedDate(header.getCreatedDate());
        dto.setUpdatedBy(header.getUpdatedBy());
        dto.setUpdatedDate(header.getUpdatedDate());

        List<VendorCalibrationDetailDto> detailsList = new ArrayList<>();
        if (header.getDetails() != null) {
            for (VendorCalibrationDetail detail : header.getDetails()) {
                VendorCalibrationDetailDto dDto = new VendorCalibrationDetailDto();
                dDto.setId(detail.getId());
                dDto.setInstrumentName(detail.getInstrumentName());
                dDto.setCapacity(detail.getCapacity());
                dDto.setDescription(detail.getDescription());
                dDto.setUsedFor(detail.getUsedFor());
                dDto.setSerialNumber(detail.getSerialNumber());
                dDto.setCalibrationCertificateNo(detail.getCalibrationCertificateNo());
                dDto.setCalibrationDate(detail.getCalibrationDate());
                dDto.setCalibrationDueDate(detail.getCalibrationDueDate());
                dDto.setCertifyingLabName(detail.getCertifyingLabName());
                dDto.setAccreditationAgency(detail.getAccreditationAgency());
                dDto.setNotificationDays(detail.getNotificationDays());
                dDto.setCalibrationStatus(detail.getCalibrationStatus());
                detailsList.add(dDto);
            }
        }
        dto.setDetails(detailsList);
        return dto;
    }


    public List<VendorCalibrationHeaderResponseDto> getByVendorCode(String vendorCode) {

        List<VendorCalibrationHeader> headers =
                headerRepository.findByVendorCode(vendorCode);

        return headers.stream()
                .map(this::mapToResponseDto)
                .toList();
    }





        @Override
        public IeVendorCalibrationInspectionResponseDto createInspection(
                CreateIeVendorCalibrationInspectionRequestDto requestDto) {

            List<VendorCalibrationHeader> vendorHeaders =
                    headerRepository.findByVendorCode(
                            requestDto.getVendorCode());

            IeVendorCalibrationInspection inspection =
                    new IeVendorCalibrationInspection();

            inspection.setCallNo(requestDto.getCallNo());
            inspection.setPoNumber(requestDto.getPoNumber());
            inspection.setVendorCode(requestDto.getVendorCode());

            List<IeVendorCalibrationInspectionDetail> inspectionDetails =
                    new ArrayList<>();

            for (VendorCalibrationHeader header : vendorHeaders) {

                if (header.getDetails() != null) {

                    for (VendorCalibrationDetail detail : header.getDetails()) {

                        IeVendorCalibrationInspectionDetail inspectionDetail =
                                new IeVendorCalibrationInspectionDetail();

                        inspectionDetail.setInstrumentName(
                                detail.getInstrumentName());

                        inspectionDetail.setCapacity(
                                detail.getCapacity());

                        inspectionDetail.setDescription(
                                detail.getDescription());

                        inspectionDetail.setUsedFor(
                                detail.getUsedFor());

                        inspectionDetail.setSerialNumber(
                                detail.getSerialNumber());

                        inspectionDetail.setCalibrationCertificateNo(
                                detail.getCalibrationCertificateNo());

                        inspectionDetail.setCalibrationDate(
                                detail.getCalibrationDate());

                        inspectionDetail.setCalibrationDueDate(
                                detail.getCalibrationDueDate());

                        inspectionDetail.setCertifyingLabName(
                                detail.getCertifyingLabName());

                        inspectionDetail.setAccreditationAgency(
                                detail.getAccreditationAgency());

                        inspectionDetail.setNotificationDays(
                                detail.getNotificationDays());

                        inspectionDetail.setCalibrationStatus(
                                detail.getCalibrationStatus());

                        inspectionDetail.setInspectionStatus(detail.getCalibrationStatus());

                        inspectionDetail.setInspection(inspection);

                        inspectionDetails.add(inspectionDetail);
                    }
                }
            }

            inspection.setDetails(inspectionDetails);

            IeVendorCalibrationInspection savedInspection =
                    inspectionRepository.save(inspection);

            return mapToResponseDto(savedInspection);
        }

        private IeVendorCalibrationInspectionResponseDto mapToResponseDto(
                IeVendorCalibrationInspection inspection) {

            IeVendorCalibrationInspectionResponseDto dto =
                    new IeVendorCalibrationInspectionResponseDto();

            dto.setId(inspection.getId());
            dto.setCallNo(inspection.getCallNo());
            dto.setPoNumber(inspection.getPoNumber());
            dto.setVendorCode(inspection.getVendorCode());

            dto.setCreatedBy(inspection.getCreatedBy());
            dto.setCreatedDate(inspection.getCreatedDate());
            dto.setUpdatedBy(inspection.getUpdatedBy());
            dto.setUpdatedDate(inspection.getUpdatedDate());

            List<IeVendorCalibrationInspectionDetailResponseDto> detailDtos =
                    new ArrayList<>();

            if (inspection.getDetails() != null) {

                for (IeVendorCalibrationInspectionDetail detail :
                        inspection.getDetails()) {

                    IeVendorCalibrationInspectionDetailResponseDto d =
                            new IeVendorCalibrationInspectionDetailResponseDto();

                    d.setId(detail.getId());
                    d.setInstrumentName(detail.getInstrumentName());
                    d.setCapacity(detail.getCapacity());
                    d.setDescription(detail.getDescription());
                    d.setUsedFor(detail.getUsedFor());
                    d.setSerialNumber(detail.getSerialNumber());
                    d.setCalibrationCertificateNo(
                            detail.getCalibrationCertificateNo());

                    d.setCalibrationDate(detail.getCalibrationDate());

                    d.setCalibrationDueDate(
                            detail.getCalibrationDueDate());

                    d.setCertifyingLabName(
                            detail.getCertifyingLabName());

                    d.setAccreditationAgency(
                            detail.getAccreditationAgency());

                    d.setNotificationDays(
                            detail.getNotificationDays());

                    d.setCalibrationStatus(
                            detail.getCalibrationStatus());

                    d.setInspectionStatus(
                            detail.getInspectionStatus());

                    d.setInspectionRemark(
                            detail.getInspectionRemark());

                    detailDtos.add(d);
                }
            }

            dto.setDetails(detailDtos);

            return dto;
        }


}
