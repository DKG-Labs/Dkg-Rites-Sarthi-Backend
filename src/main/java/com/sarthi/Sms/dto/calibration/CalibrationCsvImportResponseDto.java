package com.sarthi.Sms.dto.calibration;

import java.util.List;
import java.util.ArrayList;

public class CalibrationCsvImportResponseDto {

    private int totalRows;
    private int successfulRows;
    private int failedRows;
    private List<String> errors;
    private List<CalibrationCsvImportDto> failedRecords;

    // Constructors
    public CalibrationCsvImportResponseDto() {}

    public CalibrationCsvImportResponseDto(int totalRows) {
        this.totalRows = totalRows;
        this.successfulRows = 0;
        this.failedRows = 0;
        this.errors = new ArrayList<>();
        this.failedRecords = new ArrayList<>();
    }

    public CalibrationCsvImportResponseDto(int totalRows, int successfulRows, int failedRows,
                                          List<String> errors, List<CalibrationCsvImportDto> failedRecords) {
        this.totalRows = totalRows;
        this.successfulRows = successfulRows;
        this.failedRows = failedRows;
        this.errors = errors;
        this.failedRecords = failedRecords;
    }

    // Getters and Setters
    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

    public int getSuccessfulRows() { return successfulRows; }
    public void setSuccessfulRows(int successfulRows) { this.successfulRows = successfulRows; }

    public int getFailedRows() { return failedRows; }
    public void setFailedRows(int failedRows) { this.failedRows = failedRows; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }

    public List<CalibrationCsvImportDto> getFailedRecords() { return failedRecords; }
    public void setFailedRecords(List<CalibrationCsvImportDto> failedRecords) { this.failedRecords = failedRecords; }

    // Utility methods
    public void addSuccess() {
        this.successfulRows++;
    }

    public void addFailure(String error, CalibrationCsvImportDto record) {
        this.failedRows++;
        this.errors.add(error);
        if (record != null) {
            this.failedRecords.add(record);
        }
    }

    public void addError(String error) {
        this.errors.add(error);
    }

    public boolean hasErrors() {
        return !errors.isEmpty() || failedRows > 0;
    }

    public String getSummary() {
        return String.format("Total: %d, Success: %d, Failed: %d",
                           totalRows, successfulRows, failedRows);
    }
}
