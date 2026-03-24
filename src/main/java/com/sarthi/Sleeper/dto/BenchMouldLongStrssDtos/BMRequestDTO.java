package com.sarthi.Sleeper.dto.BenchMouldLongStrssDtos;

import lombok.Data;

import java.util.List;

@Data
public class BMRequestDTO {

    private String plantType;     // STRESS / LONG_LINE
    private String category;
    private String subCategory;
    private String drawingNo;

    private Integer createdBy;

    private List<BMDetailRequestDTO> details;
}