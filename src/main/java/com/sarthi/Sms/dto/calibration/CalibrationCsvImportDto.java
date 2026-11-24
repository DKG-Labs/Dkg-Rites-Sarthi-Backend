package com.sarthi.Sms.dto.calibration;

public class CalibrationCsvImportDto {

    private String instrumentCategory;
    private String instrument;
    private String detail;
    private String railSection;
    private String serialNumber;
    private Integer calibrationExpiryNumberOfDays;
    private String calibrationDate;
    private String calibrationResult;
    private String calibrationValidUpto;

    // Row number for error reporting
    private Integer rowNumber;

    // Validation error messages
    private String validationErrors;

    // Constructors
    public CalibrationCsvImportDto() {}

    public CalibrationCsvImportDto(String instrumentCategory, String instrument, String detail, String railSection,
                                   String serialNumber, Integer calibrationExpiryNumberOfDays, String calibrationDate,
                                   String calibrationResult, String calibrationValidUpto, Integer rowNumber, String validationErrors) {
        this.instrumentCategory = instrumentCategory;
        this.instrument = instrument;
        this.detail = detail;
        this.railSection = railSection;
        this.serialNumber = serialNumber;
        this.calibrationExpiryNumberOfDays = calibrationExpiryNumberOfDays;
        this.calibrationDate = calibrationDate;
        this.calibrationResult = calibrationResult;
        this.calibrationValidUpto = calibrationValidUpto;
        this.rowNumber = rowNumber;
        this.validationErrors = validationErrors;
    }

    // Getters and Setters
    public String getInstrumentCategory() { return instrumentCategory; }
    public void setInstrumentCategory(String instrumentCategory) { this.instrumentCategory = instrumentCategory; }

    public String getInstrument() { return instrument; }
    public void setInstrument(String instrument) { this.instrument = instrument; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getRailSection() { return railSection; }
    public void setRailSection(String railSection) { this.railSection = railSection; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public Integer getCalibrationExpiryNumberOfDays() { return calibrationExpiryNumberOfDays; }
    public void setCalibrationExpiryNumberOfDays(Integer calibrationExpiryNumberOfDays) { this.calibrationExpiryNumberOfDays = calibrationExpiryNumberOfDays; }

    public String getCalibrationDate() { return calibrationDate; }
    public void setCalibrationDate(String calibrationDate) { this.calibrationDate = calibrationDate; }

    public String getCalibrationResult() { return calibrationResult; }
    public void setCalibrationResult(String calibrationResult) { this.calibrationResult = calibrationResult; }

    public String getCalibrationValidUpto() { return calibrationValidUpto; }
    public void setCalibrationValidUpto(String calibrationValidUpto) { this.calibrationValidUpto = calibrationValidUpto; }

    public Integer getRowNumber() { return rowNumber; }
    public void setRowNumber(Integer rowNumber) { this.rowNumber = rowNumber; }

    public String getValidationErrors() { return validationErrors; }
    public void setValidationErrors(String validationErrors) { this.validationErrors = validationErrors; }

    public boolean isValid() {
        return validationErrors == null || validationErrors.isEmpty();
    }

    public void addValidationError(String error) {
        if (validationErrors == null) {
            validationErrors = error;
        } else {
            validationErrors += "; " + error;
        }
    }
}
