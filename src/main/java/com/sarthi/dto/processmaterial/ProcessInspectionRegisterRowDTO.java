package com.sarthi.dto.processmaterial;

import lombok.Data;
import java.util.List;

@Data
public class ProcessInspectionRegisterRowDTO {
    private Integer srNo;
    private String activity;
    
    // Each element in this list represents the data for one hour (column)
    // For many rows, this will be a formatted string of 3 readings (e.g. "12.5, 12.6, 12.4")
    // For Quenching/Tempering rows, it might be 4 readings or a combined string.
    private List<String> hourlyData;
    
    private String remarks; // Accepted / Not-accepted
}
