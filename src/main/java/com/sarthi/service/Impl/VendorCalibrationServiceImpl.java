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
import com.sarthi.exception.InvalidInputException;
import java.time.format.DateTimeParseException;
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
import java.util.Map;
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
                    detail.setMakeModel(detailDto.getMakeModel());
                    detail.setMasterEquipNoCertValidity(detailDto.getMasterEquipNoCertValidity());
                    detail.setMasterEquipNablDetails(detailDto.getMasterEquipNablDetails());
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
                dDto.setMakeModel(detail.getMakeModel());
                dDto.setMasterEquipNoCertValidity(detail.getMasterEquipNoCertValidity());
                dDto.setMasterEquipNablDetails(detail.getMasterEquipNablDetails());
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

            // Check if an inspection already exists for this callNo
            Optional<IeVendorCalibrationInspection> existingOpt =
                    inspectionRepository.findByCallNo(requestDto.getCallNo());

            IeVendorCalibrationInspection inspection;
            boolean isNew = false;
            if (existingOpt.isPresent()) {
                inspection = existingOpt.get();
                if (requestDto.getPoNumber() != null) {
                    inspection.setPoNumber(requestDto.getPoNumber());
                }
                if (requestDto.getVendorCode() != null) {
                    inspection.setVendorCode(requestDto.getVendorCode());
                }
            } else {
                inspection = new IeVendorCalibrationInspection();
                inspection.setCallNo(requestDto.getCallNo());
                inspection.setPoNumber(requestDto.getPoNumber());
                inspection.setVendorCode(requestDto.getVendorCode());
                isNew = true;
            }

            List<IeVendorCalibrationInspectionDetail> inspectionDetails = inspection.getDetails();
            if (inspectionDetails == null) {
                inspectionDetails = new ArrayList<>();
                inspection.setDetails(inspectionDetails);
            }

            // If request has custom verification details (IE is saving from UI)
            if (requestDto.getDetails() != null && !requestDto.getDetails().isEmpty()) {
                for (com.sarthi.dto.Calibration.IeVendorCalibrationInspectionDetailRequestDto reqDetail : requestDto.getDetails()) {
                    // Try to find matching detail in current list
                    IeVendorCalibrationInspectionDetail match = null;
                    for (IeVendorCalibrationInspectionDetail existingDetail : inspectionDetails) {
                        if (existingDetail.getInstrumentName() != null && 
                            existingDetail.getInstrumentName().equalsIgnoreCase(reqDetail.getInstrumentName()) &&
                            existingDetail.getSerialNumber() != null && 
                            existingDetail.getSerialNumber().equalsIgnoreCase(reqDetail.getSerialNumber())) {
                            match = existingDetail;
                            break;
                        }
                    }

                    if (match != null) {
                        // Update status and remark
                        match.setInspectionStatus(reqDetail.getInspectionStatus());
                        match.setInspectionRemark(reqDetail.getInspectionRemark());
                    } else {
                        // Create a new detail
                        IeVendorCalibrationInspectionDetail newDetail = new IeVendorCalibrationInspectionDetail();
                        newDetail.setInstrumentName(reqDetail.getInstrumentName());
                        newDetail.setCapacity(reqDetail.getCapacity());
                        newDetail.setSerialNumber(reqDetail.getSerialNumber());
                        newDetail.setCalibrationCertificateNo(reqDetail.getCalibrationCertificateNo());
                        newDetail.setMakeModel(reqDetail.getMakeModel());
                        newDetail.setMasterEquipNoCertValidity(reqDetail.getMasterEquipNoCertValidity());
                        newDetail.setMasterEquipNablDetails(reqDetail.getMasterEquipNablDetails());
                        newDetail.setInspectionStatus(reqDetail.getInspectionStatus());
                        newDetail.setInspectionRemark(reqDetail.getInspectionRemark());
                        newDetail.setCalibrationStatus(reqDetail.getInspectionStatus()); // fallback
                        newDetail.setInspection(inspection);
                        inspectionDetails.add(newDetail);
                    }
                }
            } else if (isNew) {
                // Initial auto-save: populate from active vendor calibrations
                List<VendorCalibrationHeader> vendorHeaders =
                        headerRepository.findByVendorCode(requestDto.getVendorCode());

                for (VendorCalibrationHeader header : vendorHeaders) {
                    if (header.getDetails() != null) {
                        for (VendorCalibrationDetail detail : header.getDetails()) {
                            IeVendorCalibrationInspectionDetail inspectionDetail =
                                    new IeVendorCalibrationInspectionDetail();

                            inspectionDetail.setInstrumentName(detail.getInstrumentName());
                            inspectionDetail.setCapacity(detail.getCapacity());
                            inspectionDetail.setDescription(detail.getDescription());
                            inspectionDetail.setUsedFor(detail.getUsedFor());
                            inspectionDetail.setSerialNumber(detail.getSerialNumber());
                            inspectionDetail.setCalibrationCertificateNo(detail.getCalibrationCertificateNo());
                            inspectionDetail.setCalibrationDate(detail.getCalibrationDate());
                            inspectionDetail.setCalibrationDueDate(detail.getCalibrationDueDate());
                            inspectionDetail.setCertifyingLabName(detail.getCertifyingLabName());
                            inspectionDetail.setAccreditationAgency(detail.getAccreditationAgency());
                            inspectionDetail.setMakeModel(detail.getMakeModel());
                            inspectionDetail.setMasterEquipNoCertValidity(detail.getMasterEquipNoCertValidity());
                            inspectionDetail.setMasterEquipNablDetails(detail.getMasterEquipNablDetails());
                            inspectionDetail.setNotificationDays(detail.getNotificationDays());
                            inspectionDetail.setCalibrationStatus(detail.getCalibrationStatus());
                            
                            // Default inspection status is same as vendor calibration status
                            inspectionDetail.setInspectionStatus(detail.getCalibrationStatus());
                            inspectionDetail.setInspection(inspection);
                            inspectionDetails.add(inspectionDetail);
                        }
                    }
                }
            }

            IeVendorCalibrationInspection savedInspection =
                    inspectionRepository.save(inspection);

            return mapToResponseDto(savedInspection);
        }

        @Override
        public IeVendorCalibrationInspectionResponseDto getInspectionByCallNo(String callNo) {
            Optional<IeVendorCalibrationInspection> inspectionOpt =
                    inspectionRepository.findByCallNo(callNo);
            
            return inspectionOpt.map(this::mapToResponseDto).orElse(null);
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

                    d.setMakeModel(detail.getMakeModel());
                    d.setMasterEquipNoCertValidity(detail.getMasterEquipNoCertValidity());
                    d.setMasterEquipNablDetails(detail.getMasterEquipNablDetails());

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

    @Override
    @Transactional
    public void submitBulkRegistration(Map<String, Object> payload, String userId) {
        try {
            logger.info("Processing bulk registration payload via service");
            String vendorCode = safeString(payload.get("vendorCode"));
            if (vendorCode == null || vendorCode.trim().isEmpty()) {
                vendorCode = userId;
            }
            if (vendorCode == null || vendorCode.trim().isEmpty()) {
                throw new InvalidInputException(new ErrorDetails(
                        AppConstant.USER_INVALID_INPUT,
                        AppConstant.ERROR_TYPE_CODE_VALIDATION,
                        AppConstant.ERROR_TYPE_VALIDATION,
                        "Vendor code is required"
                ));
            }

            String fileData = safeString(payload.get("fileData")); // Combined certificate base64
            String fileName = safeString(payload.get("fileName")); // File name

            List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");
            if (items == null || items.isEmpty()) {
                throw new InvalidInputException(new ErrorDetails(
                        AppConstant.USER_INVALID_INPUT,
                        AppConstant.ERROR_TYPE_CODE_VALIDATION,
                        AppConstant.ERROR_TYPE_VALIDATION,
                        "No calibration items provided"
                ));
            }

            // Group items by category (Document, Instrument, Gauge)
            Map<String, List<Map<String, Object>>> itemsByCategory = items.stream()
                    .filter(item -> item.get("category") != null)
                    .collect(Collectors.groupingBy(item -> safeString(item.get("category"))));

            for (Map.Entry<String, List<Map<String, Object>>> entry : itemsByCategory.entrySet()) {
                String category = entry.getKey();
                List<Map<String, Object>> categoryItems = entry.getValue();

                // Find or create VendorCalibrationHeader
                VendorCalibrationHeader header;
                Optional<VendorCalibrationHeader> existingOpt = headerRepository.findByVendorCodeAndCategory(vendorCode, category);
                if (existingOpt.isPresent()) {
                    header = existingOpt.get();
                    header.getDetails().clear();
                    headerRepository.saveAndFlush(header);
                } else {
                    header = new VendorCalibrationHeader();
                    header.setVendorCode(vendorCode);
                    header.setCategory(category);
                }

                // Set the certificate file data (since it's a combined certificate, we set it on all categories)
                header.setCertificateFilePath(fileData);
                header.setCreatedBy(userId);
                header.setUpdatedBy(userId);

                for (Map<String, Object> itemMap : categoryItems) {
                    VendorCalibrationDetail detail = new VendorCalibrationDetail();
                    detail.setInstrumentName(safeString(itemMap.get("instrumentName")));
                    detail.setCapacity(safeString(itemMap.get("capacity")));
                    detail.setDescription(safeString(itemMap.get("description")));
                    detail.setUsedFor(safeString(itemMap.get("usedFor")));
                    detail.setSerialNumber(safeString(itemMap.get("serialNumber")));
                    detail.setCalibrationCertificateNo(safeString(itemMap.get("calibrationCertificateNo")));

                    // Parse dates
                    String calDateStr = safeString(itemMap.get("calibrationDate"));
                    if (calDateStr != null && !calDateStr.trim().isEmpty()) {
                        try {
                            LocalDate calDate = LocalDate.parse(calDateStr);
                            if (calDate.getYear() < 2000 || calDate.getYear() > 2099) {
                                String instrument = safeString(itemMap.get("instrumentName"));
                                throw new InvalidInputException(new ErrorDetails(
                                        AppConstant.USER_INVALID_INPUT,
                                        AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Calibration date year must be between 2000 and 2099 for instrument '" + instrument + "'."
                                ));
                            }
                            detail.setCalibrationDate(calDate);
                        } catch (DateTimeParseException e) {
                            String instrument = safeString(itemMap.get("instrumentName"));
                            throw new InvalidInputException(new ErrorDetails(
                                    AppConstant.USER_INVALID_INPUT,
                                    AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Invalid calibration date format '" + calDateStr + "' for instrument '" + instrument + "'. Expected format is YYYY-MM-DD."
                            ));
                        }
                    }
                    String dueDateStr = safeString(itemMap.get("calibrationDueDate"));
                    LocalDate dueDate = null;
                    if (dueDateStr != null && !dueDateStr.trim().isEmpty()) {
                        try {
                            dueDate = LocalDate.parse(dueDateStr);
                            if (dueDate.getYear() < 2000 || dueDate.getYear() > 2099) {
                                String instrument = safeString(itemMap.get("instrumentName"));
                                throw new InvalidInputException(new ErrorDetails(
                                        AppConstant.USER_INVALID_INPUT,
                                        AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Calibration due date year must be between 2000 and 2099 for instrument '" + instrument + "'."
                                ));
                            }
                            detail.setCalibrationDueDate(dueDate);
                        } catch (DateTimeParseException e) {
                            String instrument = safeString(itemMap.get("instrumentName"));
                            throw new InvalidInputException(new ErrorDetails(
                                    AppConstant.USER_INVALID_INPUT,
                                    AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Invalid calibration due date format '" + dueDateStr + "' for instrument '" + instrument + "'. Expected format is YYYY-MM-DD."
                            ));
                        }
                    }

                    detail.setCertifyingLabName(safeString(itemMap.get("certifyingLabName")));
                    detail.setAccreditationAgency(safeString(itemMap.get("accreditationAgency")));
                    detail.setMakeModel(safeString(itemMap.get("makeModel")));
                    detail.setMasterEquipNoCertValidity(safeString(itemMap.get("masterEquipNoCertValidity")));
                    detail.setMasterEquipNablDetails(safeString(itemMap.get("masterEquipNablDetails")));

                    // Notification days
                    Object notifDaysObj = itemMap.get("notificationDays");
                    int notifDays = 30;
                    if (notifDaysObj instanceof Number) {
                        notifDays = ((Number) notifDaysObj).intValue();
                    } else if (notifDaysObj instanceof String) {
                        try {
                            notifDays = Integer.parseInt((String) notifDaysObj);
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                    }
                    detail.setNotificationDays(notifDays);

                    // Auto calculate status
                    String status = "Valid";
                    if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
                        status = "Expired";
                    }
                    detail.setCalibrationStatus(status);

                    detail.setCreatedBy(userId);
                    detail.setUpdatedBy(userId);

                    header.addDetail(detail);
                }

                headerRepository.save(header);
            }
        } catch (InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error processing bulk registration: {}", e.getMessage(), e);
            throw new InvalidInputException(new ErrorDetails(
                    AppConstant.USER_INVALID_INPUT,
                    AppConstant.ERROR_TYPE_CODE_VALIDATION,
                    AppConstant.ERROR_TYPE_VALIDATION,
                    "Failed to process bulk registration: " + e.getMessage()
            ));
        }
    }

    private String safeString(Object value) {
        if (value == null) return null;
        return String.valueOf(value);
    }
}
