package com.sarthi.Sms.dto.calibration;

import java.util.List;

public class BulkCalibrationCsvImportDto {

    private List<CalibrationCsvImportDto> csvRows;

    // Constructors
    public BulkCalibrationCsvImportDto() {}

    public BulkCalibrationCsvImportDto(List<CalibrationCsvImportDto> csvRows) {
        this.csvRows = csvRows;
    }

    // Getters and Setters
    public List<CalibrationCsvImportDto> getCsvRows() { 
        return csvRows; 
    }
    
    public void setCsvRows(List<CalibrationCsvImportDto> csvRows) { 
        this.csvRows = csvRows; 
    }

    public int getRowCount() {
        return csvRows != null ? csvRows.size() : 0;
    }
}
