package com.sarthi.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InspectionCallStatusDto {
    private String name; // Total, RM, Process, Final
    private long under;
    private long pending;
}
