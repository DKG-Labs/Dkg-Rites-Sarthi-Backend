package com.sarthi.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InspectionDetailsDto {
    private String name; // Total, RM, Process, Final
    private long accepted;
    private long rejected;
    private long acceptedNos;
    private long acceptedSet;
    private long rejectedNos;
    private long rejectedSet;

    public InspectionDetailsDto(String name, long accepted, long rejected) {
        this.name = name;
        this.accepted = accepted;
        this.rejected = rejected;
        this.acceptedNos = accepted;
        this.acceptedSet = 0;
        this.rejectedNos = rejected;
        this.rejectedSet = 0;
    }
}
