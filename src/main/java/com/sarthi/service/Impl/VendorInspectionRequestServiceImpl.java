package com.sarthi.service.Impl;

import com.sarthi.constant.AppConstant;
import com.sarthi.dto.RmHeatTcMappingDto;
import com.sarthi.dto.VendorInspectionRequestDto;
import com.sarthi.entity.RmHeatTcMapping;
import com.sarthi.entity.VendorInspectionRequest;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.exception.InvalidInputException;
import com.sarthi.repository.VendorInspectionRequestRepository;
import com.sarthi.repository.rawmaterial.RmIcEditRepository;
import com.sarthi.entity.rawmaterial.RmIcEdit;
import com.sarthi.service.VendorInspectionRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for Vendor Inspection Request operations.
 * Contains business logic for CRUD operations with proper error handling.
 */
@Service
@Transactional
public class VendorInspectionRequestServiceImpl implements VendorInspectionRequestService {

    private static final Logger logger = LoggerFactory.getLogger(VendorInspectionRequestServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private VendorInspectionRequestRepository inspectionRequestRepository;

    @Autowired
    private RmIcEditRepository rmIcEditRepository;

    @Override
    public VendorInspectionRequestDto createInspectionRequest(VendorInspectionRequestDto requestDto) {
        logger.info("Creating new inspection request for PO: {}", requestDto.getPoNo());

        // Validate required fields
        validateInspectionRequest(requestDto);

        // Check for duplicate PO serial number
        if (requestDto.getPoSerialNo() != null && inspectionRequestRepository.existsByPoSerialNo(requestDto.getPoSerialNo())) {
            throw new InvalidInputException(new ErrorDetails(
                AppConstant.USER_INVALID_INPUT,
                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                AppConstant.ERROR_TYPE_VALIDATION,
                "Inspection request with PO Serial No '" + requestDto.getPoSerialNo() + "' already exists."
            ));
        }

        // Map DTO to Entity
        VendorInspectionRequest entity = mapDtoToEntity(requestDto);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setStatus("PENDING");

        // Map and add heat TC mappings
        if (requestDto.getRmHeatTcMapping() != null && !requestDto.getRmHeatTcMapping().isEmpty()) {
            for (RmHeatTcMappingDto mappingDto : requestDto.getRmHeatTcMapping()) {
                RmHeatTcMapping mapping = mapHeatTcDtoToEntity(mappingDto);
                entity.addHeatTcMapping(mapping);
            }
        }

        // Save entity
        VendorInspectionRequest savedEntity = inspectionRequestRepository.save(entity);
        logger.info("Inspection request created successfully with ID: {}", savedEntity.getId());

        return mapEntityToDto(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public VendorInspectionRequestDto getInspectionRequestById(Long id) {
        logger.info("Fetching inspection request by ID: {}", id);

        VendorInspectionRequest entity = inspectionRequestRepository.findById(id)
            .orElseThrow(() -> new BusinessException(new ErrorDetails(
                AppConstant.ERROR_CODE_RESOURCE,
                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                AppConstant.ERROR_TYPE_RESOURCE,
                "Inspection request not found with ID: " + id
            )));

        return mapEntityToDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public VendorInspectionRequestDto getInspectionRequestByPoSerialNo(String poSerialNo) {
        logger.info("Fetching inspection request by PO Serial No: {}", poSerialNo);

        VendorInspectionRequest entity = inspectionRequestRepository.findByPoSerialNo(poSerialNo)
            .orElseThrow(() -> new BusinessException(new ErrorDetails(
                AppConstant.ERROR_CODE_RESOURCE,
                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                AppConstant.ERROR_TYPE_RESOURCE,
                "Inspection request not found with PO Serial No: " + poSerialNo
            )));

        return mapEntityToDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorInspectionRequestDto> getAllInspectionRequests() {
        logger.info("Fetching all inspection requests");
        return inspectionRequestRepository.findAllByOrderByCreatedAtDesc()
            .stream()
            .map(this::mapEntityToDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorInspectionRequestDto> getInspectionRequestsByStatus(String status) {
        logger.info("Fetching inspection requests by status: {}", status);
        return inspectionRequestRepository.findByStatusOrderByCreatedAtDesc(status)
            .stream()
            .map(this::mapEntityToDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorInspectionRequestDto> getInspectionRequestsByTypeOfCall(String typeOfCall) {
        logger.info("Fetching inspection requests by type of call: {}", typeOfCall);
        return inspectionRequestRepository.findByTypeOfCall(typeOfCall)
            .stream()
            .map(this::mapEntityToDto)
            .collect(Collectors.toList());
    }

    @Override
    public VendorInspectionRequestDto updateInspectionRequestStatus(Long id, String status, String updatedBy) {
        logger.info("Updating status for inspection request ID: {} to {}", id, status);

        VendorInspectionRequest entity = inspectionRequestRepository.findById(id)
            .orElseThrow(() -> new BusinessException(new ErrorDetails(
                AppConstant.ERROR_CODE_RESOURCE,
                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                AppConstant.ERROR_TYPE_RESOURCE,
                "Inspection request not found with ID: " + id
            )));

        entity.setStatus(status);
        entity.setUpdatedBy(updatedBy);
        entity.setUpdatedAt(LocalDateTime.now());

        VendorInspectionRequest updatedEntity = inspectionRequestRepository.save(entity);
        logger.info("Status updated successfully for inspection request ID: {}", id);

        return mapEntityToDto(updatedEntity);
    }

    @Override
    public void deleteInspectionRequest(Long id) {
        logger.info("Deleting inspection request ID: {}", id);

        if (!inspectionRequestRepository.existsById(id)) {
            throw new BusinessException(new ErrorDetails(
                AppConstant.ERROR_CODE_RESOURCE,
                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                AppConstant.ERROR_TYPE_RESOURCE,
                "Inspection request not found with ID: " + id
            ));
        }

        inspectionRequestRepository.deleteById(id);
        logger.info("Inspection request deleted successfully with ID: {}", id);
    }

    // ==================== Private Helper Methods ====================

    /**
     * Validates required fields in inspection request
     */
    private void validateInspectionRequest(VendorInspectionRequestDto dto) {
        if (dto.getPoNo() == null || dto.getPoNo().trim().isEmpty()) {
            throw new InvalidInputException(new ErrorDetails(
                AppConstant.USER_INVALID_INPUT,
                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                AppConstant.ERROR_TYPE_VALIDATION,
                "PO Number is required."
            ));
        }

        if (dto.getTypeOfCall() == null || dto.getTypeOfCall().trim().isEmpty()) {
            throw new InvalidInputException(new ErrorDetails(
                AppConstant.USER_INVALID_INPUT,
                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                AppConstant.ERROR_TYPE_VALIDATION,
                "Type of Call is required."
            ));
        }
    }

    /**
     * Maps VendorInspectionRequestDto to VendorInspectionRequest entity
     */
    private VendorInspectionRequest mapDtoToEntity(VendorInspectionRequestDto dto) {
        VendorInspectionRequest entity = new VendorInspectionRequest();

        entity.setPoNo(dto.getPoNo());
        entity.setPoSerialNo(dto.getPoSerialNo());
        entity.setPoDate(parseDate(dto.getPoDate()));
        entity.setPoDescription(dto.getPoDescription());
        entity.setPoQty(dto.getPoQty());
        entity.setPoUnit(dto.getPoUnit());
        entity.setAmendmentNo(dto.getAmendmentNo());
        entity.setAmendmentDate(parseDate(dto.getAmendmentDate()));

        entity.setVendorCode(dto.getVendorCode());
        entity.setVendorContactName(dto.getVendorContactName());
        entity.setVendorContactPhone(dto.getVendorContactPhone());

        entity.setTypeOfCall(dto.getTypeOfCall());
        entity.setDesiredInspectionDate(parseDate(dto.getDesiredInspectionDate()));

        entity.setQtyAlreadyInspectedRm(dto.getQtyAlreadyInspectedRm() != null ? dto.getQtyAlreadyInspectedRm() : 0);
        entity.setQtyAlreadyInspectedProcess(dto.getQtyAlreadyInspectedProcess() != null ? dto.getQtyAlreadyInspectedProcess() : 0);
        entity.setQtyAlreadyInspectedFinal(dto.getQtyAlreadyInspectedFinal() != null ? dto.getQtyAlreadyInspectedFinal() : 0);

        entity.setRmHeatNumbers(dto.getRmHeatNumbers());

        entity.setRmChemicalCarbon(dto.getRmChemicalCarbon());
        entity.setRmChemicalManganese(dto.getRmChemicalManganese());
        entity.setRmChemicalSilicon(dto.getRmChemicalSilicon());
        entity.setRmChemicalSulphur(dto.getRmChemicalSulphur());
        entity.setRmChemicalPhosphorus(dto.getRmChemicalPhosphorus());
        entity.setRmChemicalChromium(dto.getRmChemicalChromium());

        entity.setRmTotalOfferedQtyMt(dto.getRmTotalOfferedQtyMt());
        entity.setRmOfferedQtyErc(dto.getRmOfferedQtyErc());

        entity.setCompanyId(dto.getCompanyId());
        entity.setCompanyName(dto.getCompanyName());
        entity.setCin(dto.getCin());

        entity.setUnitId(dto.getUnitId());
        entity.setUnitName(dto.getUnitName());
        entity.setUnitAddress(dto.getUnitAddress());
        entity.setUnitGstin(dto.getUnitGstin());
        entity.setUnitContactPerson(dto.getUnitContactPerson());
        entity.setUnitRole(dto.getUnitRole());

        // PO Additional Information
        entity.setPurchasingAuthority(dto.getPurchasingAuthority());
        entity.setBpo(dto.getBpo());
        entity.setDeliveryPeriod(dto.getDeliveryPeriod());
        entity.setInspectionFeesPaymentDetails(dto.getInspectionFeesPaymentDetails());

        entity.setRemarks(dto.getRemarks());
        entity.setCreatedBy(dto.getCreatedBy());

        return entity;
    }

    /**
     * Maps RmHeatTcMappingDto to RmHeatTcMapping entity
     */
    private RmHeatTcMapping mapHeatTcDtoToEntity(RmHeatTcMappingDto dto) {
        RmHeatTcMapping entity = new RmHeatTcMapping();

        entity.setHeatNumber(dto.getHeatNumber());
        entity.setTcNumber(dto.getTcNumber());
        entity.setTcDate(parseDate(dto.getTcDate()));
        entity.setManufacturer(dto.getManufacturer());

        entity.setInvoiceNo(dto.getInvoiceNo());
        entity.setInvoiceDate(parseDate(dto.getInvoiceDate()));

        entity.setSubPoNumber(dto.getSubPoNumber());
        entity.setSubPoDate(parseDate(dto.getSubPoDate()));
        entity.setSubPoQty(dto.getSubPoQty());
        entity.setSubPoTotalValue(dto.getSubPoTotalValue());

        entity.setTcQty(dto.getTcQty());
        entity.setTcQtyRemaining(dto.getTcQtyRemaining());
        entity.setOfferedQty(dto.getOfferedQty());

        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        return entity;
    }

    /**
     * Maps VendorInspectionRequest entity to VendorInspectionRequestDto
     */
    private VendorInspectionRequestDto mapEntityToDto(VendorInspectionRequest entity) {
        VendorInspectionRequestDto dto = new VendorInspectionRequestDto();

        dto.setId(entity.getId());
        dto.setPoNo(entity.getPoNo());
        dto.setPoSerialNo(entity.getPoSerialNo());
        dto.setPoDate(formatDate(entity.getPoDate()));
        dto.setPoDescription(entity.getPoDescription());
        dto.setPoQty(entity.getPoQty());
        dto.setPoUnit(entity.getPoUnit());
        dto.setAmendmentNo(entity.getAmendmentNo());
        dto.setAmendmentDate(formatDate(entity.getAmendmentDate()));

        dto.setVendorCode(entity.getVendorCode());
        dto.setVendorContactName(entity.getVendorContactName());
        dto.setVendorContactPhone(entity.getVendorContactPhone());

        dto.setTypeOfCall(entity.getTypeOfCall());
        dto.setDesiredInspectionDate(formatDate(entity.getDesiredInspectionDate()));

        dto.setQtyAlreadyInspectedRm(entity.getQtyAlreadyInspectedRm());
        dto.setQtyAlreadyInspectedProcess(entity.getQtyAlreadyInspectedProcess());
        dto.setQtyAlreadyInspectedFinal(entity.getQtyAlreadyInspectedFinal());

        dto.setRmHeatNumbers(entity.getRmHeatNumbers());

        dto.setRmChemicalCarbon(entity.getRmChemicalCarbon());
        dto.setRmChemicalManganese(entity.getRmChemicalManganese());
        dto.setRmChemicalSilicon(entity.getRmChemicalSilicon());
        dto.setRmChemicalSulphur(entity.getRmChemicalSulphur());
        dto.setRmChemicalPhosphorus(entity.getRmChemicalPhosphorus());
        dto.setRmChemicalChromium(entity.getRmChemicalChromium());

        dto.setRmTotalOfferedQtyMt(entity.getRmTotalOfferedQtyMt());
        dto.setRmOfferedQtyErc(entity.getRmOfferedQtyErc());

        dto.setCompanyId(entity.getCompanyId());
        dto.setCompanyName(entity.getCompanyName());
        dto.setCin(entity.getCin());

        dto.setUnitId(entity.getUnitId());
        dto.setUnitName(entity.getUnitName());
        dto.setUnitAddress(entity.getUnitAddress());
        dto.setUnitGstin(entity.getUnitGstin());
        dto.setUnitContactPerson(entity.getUnitContactPerson());
        dto.setUnitRole(entity.getUnitRole());

        // PO Additional Information
        dto.setPurchasingAuthority(entity.getPurchasingAuthority());
        dto.setBpo(entity.getBpo());
        dto.setDeliveryPeriod(entity.getDeliveryPeriod());
        dto.setInspectionFeesPaymentDetails(entity.getInspectionFeesPaymentDetails());

        dto.setRemarks(entity.getRemarks());
        dto.setStatus(entity.getStatus());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null);
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : null);

        // Map heat TC mappings
        if (entity.getRmHeatTcMappings() != null && !entity.getRmHeatTcMappings().isEmpty()) {
            List<RmHeatTcMappingDto> mappingDtos = entity.getRmHeatTcMappings().stream()
                .map(this::mapHeatTcEntityToDto)
                .collect(Collectors.toList());
            dto.setRmHeatTcMapping(mappingDtos);
        } else {
            dto.setRmHeatTcMapping(new ArrayList<>());
        }

        return dto;
    }

    /**
     * Maps RmHeatTcMapping entity to RmHeatTcMappingDto
     */
    private RmHeatTcMappingDto mapHeatTcEntityToDto(RmHeatTcMapping entity) {
        RmHeatTcMappingDto dto = new RmHeatTcMappingDto();

        dto.setId(entity.getId());
        dto.setHeatNumber(entity.getHeatNumber());
        dto.setTcNumber(entity.getTcNumber());
        dto.setTcDate(formatDate(entity.getTcDate()));
        dto.setManufacturer(entity.getManufacturer());

        dto.setInvoiceNo(entity.getInvoiceNo());
        dto.setInvoiceDate(formatDate(entity.getInvoiceDate()));

        dto.setSubPoNumber(entity.getSubPoNumber());
        
        String rmIcDateStr = null;
        if (entity.getSubPoNumber() != null && !entity.getSubPoNumber().trim().isEmpty()) {
            java.util.Optional<RmIcEdit> editOpt = rmIcEditRepository.findByIcNumber(entity.getSubPoNumber().trim());
            if (editOpt.isPresent() && editOpt.get().getCreatedAt() != null) {
                rmIcDateStr = editOpt.get().getCreatedAt().toLocalDate().toString();
            }
        }
        if (rmIcDateStr != null) {
            dto.setSubPoDate(rmIcDateStr);
        } else {
            dto.setSubPoDate(formatDate(entity.getSubPoDate()));
        }

        dto.setSubPoQty(entity.getSubPoQty());
        dto.setSubPoTotalValue(entity.getSubPoTotalValue());

        dto.setTcQty(entity.getTcQty());
        dto.setTcQtyRemaining(entity.getTcQtyRemaining());
        dto.setOfferedQty(entity.getOfferedQty());

        return dto;
    }

    /**
     * Parses date string to LocalDate
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            logger.warn("Unable to parse date: {}", dateStr);
            return null;
        }
    }

    /**
     * Formats LocalDate to string
     */
    private String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }
}

