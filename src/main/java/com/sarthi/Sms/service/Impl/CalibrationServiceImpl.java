package com.sarthi.Sms.service.Impl;

import com.sarthi.Sms.constant.RailMillAppConstant;
import com.sarthi.Sms.dto.calibration.BulkCalibrationReqDto;
import com.sarthi.Sms.dto.calibration.CalibrationDetailDto;
import com.sarthi.Sms.dto.calibration.CalibrationReqDto;
import com.sarthi.Sms.dto.calibration.CalibrationCsvImportDto;
import com.sarthi.Sms.dto.calibration.CalibrationCsvImportResponseDto;
import com.sarthi.Sms.dto.sms.common.DutyStatusResDto;
import com.sarthi.Sms.dto.sms.common.EndDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyResDto;
import com.sarthi.Sms.entity.calibration.CalibrationDetailEntity;
import com.sarthi.Sms.entity.calibration.CalibrationDetailId;
import com.sarthi.Sms.entity.calibration.CalibrationDutyEntity;
import com.sarthi.Sms.exception.SmsErrorDetails;
import com.sarthi.Sms.exception.SmsInvalidArgumentException;
import com.sarthi.Sms.exception.SmsResourceNotFoundException;
import com.sarthi.Sms.repository.calibration.CalibrationDetailRepository;
import com.sarthi.Sms.repository.calibration.CalibrationDutyRepository;
import com.sarthi.Sms.service.CalibrationService;
import com.sarthi.Sms.service.DutySequenceService;
import com.sarthi.Sms.util.RailMillCommonUtils;
import com.sarthi.Sms.util.DutyEnum;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CalibrationServiceImpl implements CalibrationService {

    @Autowired
    private CalibrationDutyRepository cdr;

    @Autowired
    private DutySequenceService dss;

    @Autowired
    private CalibrationDetailRepository calibrationDetailRepository;

    @Override
    @Transactional
    public StartDutyResDto startDuty(String ah, StartDutyReqDto req) {
        CalibrationDutyEntity ce = new CalibrationDutyEntity();
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(ah);

        // Check if a duty is already in progress
        DutyStatusResDto dsrd = checkDutyStatus(ah);
        if (dsrd.getIsDutyInProgess()) {
            throw new SmsInvalidArgumentException(new SmsErrorDetails(RailMillAppConstant.USER_ALREADY_EXISTS,
                    RailMillAppConstant.USER_ALREADY_EXISTS, RailMillAppConstant.ERROR_TYPE_VALIDATION,
                    "Cannot start new duty for the user. Previous duty already in progress."));
        }

        LocalDate date = RailMillCommonUtils.convertStringToDateObject(req.getStartDate());

        // Generate a unique duty ID
        String dutyId = dss.generateDutyId(DutyEnum.CLB, date);
        ce.setDutyId(dutyId);
        ce.setUserId(userId);
        ce.setDate(date);
        ce.setShift(req.getShift());

        ce.setStartTime(LocalDateTime.now());
        ce = cdr.save(ce);

        StartDutyResDto sdrd = new StartDutyResDto();
        sdrd.setDate(RailMillCommonUtils.convertDateToString(ce.getDate()));
        sdrd.setDutyId(ce.getDutyId());
        sdrd.setShift(ce.getShift());
        sdrd.setStartTime(RailMillCommonUtils.extractTime(ce.getStartTime()));

        return sdrd;
    }

    @Override
    @Transactional
    public void endDuty(EndDutyReqDto req) {
        CalibrationDutyEntity cde = cdr.findByDutyId(req.getDutyId())
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(RailMillAppConstant.ERROR_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_RESOURCE,
                                "No duties found for the provided duty id.")));

        if (Objects.nonNull(cde.getEndTime())) {
            throw new SmsInvalidArgumentException(
                    new SmsErrorDetails(
                            RailMillAppConstant.ERROR_CODE_VALIDATION,
                            RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                            RailMillAppConstant.ERROR_TYPE_VALIDATION,
                            "Duty already ended."));
        }

        cde.setShiftRemarks(req.getShiftRemarks());
        cde.setEndTime(LocalDateTime.now());
        cdr.save(cde);
    }

    @Override
    public DutyStatusResDto checkDutyStatus(String ah) {
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(ah);
        String dutyId = cdr.checkDutyStatus(userId);
        DutyStatusResDto dsrd = new DutyStatusResDto();
        dsrd.setDutyId(dutyId);
        dsrd.setIsDutyInProgess(dutyId != null);

        return dsrd;
    }

    @Override
    public StartDutyResDto getOngoingDutyDtls(String ah) {
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(ah);
        Optional<CalibrationDutyEntity> rdeOpt = cdr.getOngoingDutyDtls(userId);

        StartDutyResDto sdrd = new StartDutyResDto();
        if (rdeOpt.isPresent()) {
            CalibrationDutyEntity rde = rdeOpt.get();
            sdrd.setDate(RailMillCommonUtils.convertDateToString(rde.getDate()));
            sdrd.setShift(rde.getShift());
            sdrd.setDutyId(rde.getDutyId());
            sdrd.setStartTime(RailMillCommonUtils.extractTime(rde.getStartTime()));
        }

        return sdrd;
    }

    @Override
    @Transactional
    public void saveCalibration(String authHeader, CalibrationReqDto request) {
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(authHeader);

        CalibrationDutyEntity calibrationDutyEntity = cdr.findById(request.getDutyId())
                .orElseThrow(() -> new SmsInvalidArgumentException(new SmsErrorDetails(
                        RailMillAppConstant.ERROR_CODE_VALIDATION,
                        RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                        RailMillAppConstant.ERROR_TYPE_VALIDATION,
                        "Invalid Duty ID provided.")));

        if (!calibrationDutyEntity.getUserId().equals(userId)) {
            throw new SmsInvalidArgumentException(new SmsErrorDetails(
                    RailMillAppConstant.ERROR_CODE_VALIDATION,
                    RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                    RailMillAppConstant.ERROR_TYPE_VALIDATION,
                    "You are not authorized to add calibration for this duty ID."));
        }

        CalibrationDetailEntity calibrationEntity = new CalibrationDetailEntity();
        CalibrationDetailId detailId = new CalibrationDetailId();
        detailId.setDutyId(request.getDutyId());
        detailId.setSerialNumber(request.getSerialNumber());
        calibrationEntity.setId(detailId);

        calibrationEntity.setInstrumentCategory(request.getInstrumentCategory());
        calibrationEntity.setInstrument(request.getInstrument());
        calibrationEntity.setDetail(request.getDetail());
        calibrationEntity.setRailSection(request.getRailSection());

        calibrationEntity.setNotificationAfterDays(request.getCalibrationExpiryNumberOfDays());
        calibrationEntity.setCalibrationDate(convertDateString(request.getCalibrationDate()));
        calibrationEntity.setResult(request.getCalibrationResult());
        if(Objects.nonNull(request.getCalibrationValidUpto())){
            calibrationEntity.setCalibrationValidUpto(convertDateString(request.getCalibrationValidUpto()));
        }

        // Save entity
        calibrationDetailRepository.save(calibrationEntity);



    }

    @Override
    public CalibrationDetailDto getLatestCalibrationDetail(String serialNumber) {
        CalibrationDetailEntity calibrationDetail = calibrationDetailRepository
                .findTopByIdSerialNumberOrderByCalibrationDateDesc(serialNumber)
                .orElseThrow(() ->  new SmsInvalidArgumentException(new SmsErrorDetails(
                RailMillAppConstant.ERROR_CODE_VALIDATION,
                RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                RailMillAppConstant.ERROR_TYPE_VALIDATION,
                "No details found for the provided serial number.")));

        return CalibrationDetailDto.builder()
                .instrumentCategory(calibrationDetail.getInstrumentCategory())
                .instrument(calibrationDetail.getInstrument())
                .detail(calibrationDetail.getDetail())
                .railSection(calibrationDetail.getRailSection())
                .serialNumber(calibrationDetail.getId().getSerialNumber())
                .calibrationValidUpto(RailMillCommonUtils.convertDateToString(calibrationDetail.getCalibrationValidUpto()))
                .notificationAfterDays(calibrationDetail.getNotificationAfterDays())
                .build();
    }

    @Override
    public List<CalibrationDetailDto> getAllLatestCalibrations() {
        List<CalibrationDetailEntity> latestCalibrations = calibrationDetailRepository.findAllLatestCalibrations();

        return latestCalibrations.stream()
                .map(calibration -> CalibrationDetailDto.builder()
                        .instrumentCategory(calibration.getInstrumentCategory())
                        .instrument(calibration.getInstrument())
                        .detail(calibration.getDetail())
                        .railSection(calibration.getRailSection())
                        .serialNumber(calibration.getId().getSerialNumber())
                        .calibrationValidUpto(RailMillCommonUtils.convertDateToString(calibration.getCalibrationValidUpto()))
                        .notificationAfterDays(calibration.getNotificationAfterDays())
                        .calibrationDate(RailMillCommonUtils.convertDateToString(calibration.getCalibrationDate()))
                        .result(calibration.getResult())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveBulkCalibration( String authHeader,BulkCalibrationReqDto req) {
        for(String serialNumber : req.getSerialNumberList()){

            CalibrationDetailEntity cde1 = calibrationDetailRepository
                .findTopByIdSerialNumberOrderByCalibrationDateDesc(serialNumber)
                .orElseThrow(() ->  new SmsResourceNotFoundException(new SmsErrorDetails(
                RailMillAppConstant.ERROR_CODE_RESOURCE,
                RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                RailMillAppConstant.ERROR_TYPE_RESOURCE,
                "No details found for the provided serial number.")));

            CalibrationDetailEntity cde = new CalibrationDetailEntity();

            cde.setCalibrationDate(LocalDate.now());
            if(Objects.nonNull(req.getCalibrationValidUpto())){
                cde.setCalibrationValidUpto(RailMillCommonUtils.convertStringToDateObject(req.getCalibrationValidUpto()));
            }

            cde.setDetail(req.getDetail());

            cde.setId(new CalibrationDetailId(serialNumber, req.getDutyId()));

            cde.setInstrument(cde1.getInstrument());

            cde.setInstrumentCategory(cde1.getInstrumentCategory());

            if(Objects.nonNull(req.getCalibrationExpiryNumberOfDays())){
                cde.setNotificationAfterDays(req.getCalibrationExpiryNumberOfDays());
            }

            cde.setRailSection(cde1.getRailSection());

            cde.setResult(req.getCalibrationResult());

            calibrationDetailRepository.save(cde);

        }
    }


    @Override
    public Map<String, List<String> > getMmVrnrWmcInstList(){
        List<String> targetInstruments = Arrays.asList(
            "Micrometer",
            "Vernier",
            "Weighing Scale"
        );

        List<CalibrationDetailEntity> allMatches =
        calibrationDetailRepository.findByInstrumentIn(targetInstruments);

        Map<String, List<String>> resultMap = allMatches.stream()
        .collect(Collectors.groupingBy(
            cd -> cd.getInstrument(),
            Collectors.mapping(cd -> cd.getId().getSerialNumber(), Collectors.toList())
        ));

        return resultMap;
    }

    @Override
    @Transactional
    public void deleteCalibration(String serialNumber) {
        // Find the latest calibration record for the given serial number
        CalibrationDetailEntity calibrationDetail = calibrationDetailRepository
                .findTopByIdSerialNumberOrderByCalibrationDateDesc(serialNumber)
                .orElseThrow(() -> new SmsResourceNotFoundException(new SmsErrorDetails(
                        RailMillAppConstant.ERROR_CODE_RESOURCE,
                        RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                        RailMillAppConstant.ERROR_TYPE_RESOURCE,
                        "No calibration record found for the provided serial number.")));

        // Delete the calibration record
        calibrationDetailRepository.delete(calibrationDetail);
    }

    @Override
    public boolean isExistingCalibration(String serialNumber) {
        return calibrationDetailRepository.findTopByIdSerialNumberOrderByCalibrationDateDesc(serialNumber).isPresent();
    }

    @Override
    @Transactional
    public CalibrationCsvImportResponseDto importCalibrationFromCsv(String authHeader, CalibrationCsvImportDto csvData) {
        CalibrationCsvImportResponseDto response = new CalibrationCsvImportResponseDto(1);

        // Get user ID from auth header
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(authHeader);

        // Use fixed duty ID for CSV imports
        String dutyId = "CLB010925001";

        // Set row number for single row
        csvData.setRowNumber(1);

        try {
            // Validate CSV row
            if (!validateCsvRow(csvData)) {
                String errorMsg = "Validation failed: " + csvData.getValidationErrors();
                System.out.println("CSV Validation Error: " + errorMsg);
                response.addFailure(errorMsg, csvData);
                return response;
            }

            // Convert CSV row to CalibrationReqDto
            CalibrationReqDto calibrationReq = convertCsvToCalibrationReq(csvData, dutyId);

            // Save calibration using existing method
            saveCalibration(authHeader, calibrationReq);

            response.addSuccess();

        } catch (Exception e) {
            String errorMsg = "Import failed: " + e.getMessage();
            System.out.println("CSV Import Exception: " + errorMsg);
            e.printStackTrace();
            response.addFailure(errorMsg, csvData);
        }

        return response;
    }

    @Override
    @Transactional
    public CalibrationCsvImportResponseDto importBulkCalibrationFromCsv(String authHeader, com.sarthi.Sms.dto.calibration.BulkCalibrationCsvImportDto bulkCsvData) {
        List<CalibrationCsvImportDto> csvRows = bulkCsvData.getCsvRows();
        CalibrationCsvImportResponseDto response = new CalibrationCsvImportResponseDto(csvRows.size());

        // Use fixed duty ID for CSV imports
        String dutyId = "CLB010925001";

        System.out.println("Processing bulk CSV import with " + csvRows.size() + " rows");

        for (int i = 0; i < csvRows.size(); i++) {
            CalibrationCsvImportDto csvRow = csvRows.get(i);
            csvRow.setRowNumber(i + 1);

            try {
                // Validate CSV row
                if (!validateCsvRow(csvRow)) {
                    String errorMsg = "Row " + (i + 1) + " validation failed: " + csvRow.getValidationErrors();
                    System.out.println("CSV Validation Error: " + errorMsg);
                    response.addFailure(errorMsg, csvRow);
                    continue;
                }

                // Convert CSV row to CalibrationReqDto
                CalibrationReqDto calibrationReq = convertCsvToCalibrationReq(csvRow, dutyId);

                // Save calibration using existing method
                saveCalibration(authHeader, calibrationReq);

                response.addSuccess();
                System.out.println("Successfully processed row " + (i + 1));

            } catch (Exception e) {
                String errorMsg = "Row " + (i + 1) + " import failed: " + e.getMessage();
                System.out.println("CSV Import Exception: " + errorMsg);
                e.printStackTrace();
                response.addFailure(errorMsg, csvRow);
            }
        }

        System.out.println("Bulk CSV import completed. " + response.getSummary());
        return response;
    }

    private boolean validateCsvRow(CalibrationCsvImportDto csvRow) {
        boolean isValid = true;

        // Validate required fields
        if (csvRow.getInstrumentCategory() == null || csvRow.getInstrumentCategory().trim().isEmpty()) {
            csvRow.addValidationError("Instrument Category is required");
            isValid = false;
        }

        if (csvRow.getInstrument() == null || csvRow.getInstrument().trim().isEmpty()) {
            csvRow.addValidationError("Instrument is required");
            isValid = false;
        }

        if (csvRow.getDetail() == null || csvRow.getDetail().trim().isEmpty()) {
            csvRow.addValidationError("Detail is required");
            isValid = false;
        }

        if (csvRow.getSerialNumber() == null || csvRow.getSerialNumber().trim().isEmpty()) {
            csvRow.addValidationError("Serial Number is required");
            isValid = false;
        }

        if (csvRow.getCalibrationExpiryNumberOfDays() == null || csvRow.getCalibrationExpiryNumberOfDays() < 1) {
            csvRow.addValidationError("Calibration Expiry Number of Days must be at least 1");
            isValid = false;
        }

        if (csvRow.getCalibrationDate() == null || csvRow.getCalibrationDate().trim().isEmpty()) {
            csvRow.addValidationError("Calibration Date is required");
            isValid = false;
        } else if (!isValidDateFormat(csvRow.getCalibrationDate())) {
            csvRow.addValidationError("Calibration Date must be in format YYYY-MM-DD, DD-MM-YYYY, or DD/MM/YYYY");
            isValid = false;
        }

        if (csvRow.getCalibrationResult() == null || csvRow.getCalibrationResult().trim().isEmpty()) {
            csvRow.addValidationError("Calibration Result is required");
            isValid = false;
        } else if (!Arrays.asList("OK", "Not OK", "Discarded").contains(csvRow.getCalibrationResult())) {
            csvRow.addValidationError("Calibration Result must be one of: OK, Not OK, Discarded");
            isValid = false;
        }

        if (csvRow.getCalibrationValidUpto() == null || csvRow.getCalibrationValidUpto().trim().isEmpty()) {
            csvRow.addValidationError("Calibration Valid Upto is required");
            isValid = false;
        } else if (!isValidDateFormat(csvRow.getCalibrationValidUpto())) {
            csvRow.addValidationError("Calibration Valid Upto must be in format YYYY-MM-DD, DD-MM-YYYY, or DD/MM/YYYY");
            isValid = false;
        }

        return isValid;
    }

    private CalibrationReqDto convertCsvToCalibrationReq(CalibrationCsvImportDto csvRow, String dutyId) {
        CalibrationReqDto calibrationReq = new CalibrationReqDto();
        calibrationReq.setDutyId(dutyId);
        calibrationReq.setInstrumentCategory(csvRow.getInstrumentCategory());
        calibrationReq.setInstrument(csvRow.getInstrument());
        calibrationReq.setDetail(csvRow.getDetail());
        calibrationReq.setRailSection(csvRow.getRailSection());
        calibrationReq.setSerialNumber(csvRow.getSerialNumber());
        calibrationReq.setCalibrationExpiryNumberOfDays(csvRow.getCalibrationExpiryNumberOfDays());
        calibrationReq.setCalibrationDate(csvRow.getCalibrationDate());
        calibrationReq.setCalibrationResult(csvRow.getCalibrationResult());
        calibrationReq.setCalibrationValidUpto(csvRow.getCalibrationValidUpto());
        return calibrationReq;
    }

    /**
     * Convert date string from CSV format (yyyy-MM-dd) to LocalDate
     * Also handles the standard dd/MM/yyyy format
     */
    private LocalDate convertDateString(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            throw new SmsInvalidArgumentException(new SmsErrorDetails(
                RailMillAppConstant.INVALID_DATE_CODE,
                RailMillAppConstant.INVALID_DATE_TYPE,
                RailMillAppConstant.ERROR_TYPE_ERROR,
                "Date string cannot be null or empty"));
        }

        try {
            // First try CSV format (yyyy-MM-dd)
            if (dateString.matches("\\d{4}-\\d{2}-\\d{2}")) {
                DateTimeFormatter csvFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                return LocalDate.parse(dateString, csvFormatter);
            }
            // Then try Excel format (dd-MM-yyyy)
            else if (dateString.matches("\\d{2}-\\d{2}-\\d{4}")) {
                DateTimeFormatter excelFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                return LocalDate.parse(dateString, excelFormatter);
            }
            // Then try standard format (dd/MM/yyyy)
            else if (dateString.matches("\\d{2}/\\d{2}/\\d{4}")) {
                DateTimeFormatter standardFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return LocalDate.parse(dateString, standardFormatter);
            }
            else {
                throw new SmsInvalidArgumentException(new SmsErrorDetails(
                    RailMillAppConstant.INVALID_DATE_CODE,
                    RailMillAppConstant.INVALID_DATE_TYPE,
                    RailMillAppConstant.ERROR_TYPE_ERROR,
                    "Date must be in format yyyy-MM-dd, dd-MM-yyyy, or dd/MM/yyyy"));
            }
        } catch (Exception e) {
            throw new SmsInvalidArgumentException(new SmsErrorDetails(
                RailMillAppConstant.INVALID_DATE_CODE,
                RailMillAppConstant.INVALID_DATE_TYPE,
                RailMillAppConstant.ERROR_TYPE_ERROR,
                "Invalid date format. Expected yyyy-MM-dd or dd/MM/yyyy"));
        }
    }

    /**
     * Helper method to validate date format
     * Accepts yyyy-MM-dd, dd-MM-yyyy, or dd/MM/yyyy formats
     */
    private boolean isValidDateFormat(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return false;
        }

        // Check for yyyy-MM-dd format
        if (dateString.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return true;
        }

        // Check for dd-MM-yyyy format
        if (dateString.matches("\\d{2}-\\d{2}-\\d{4}")) {
            return true;
        }

        // Check for dd/MM/yyyy format
        if (dateString.matches("\\d{2}/\\d{2}/\\d{4}")) {
            return true;
        }

        return false;
    }

}
