package com.sarthi.Sleeper.dto.BenchMouldLongStrssDtos;

import lombok.Data;

import java.util.Date;
import java.util.List;


@Data
public class BMResponseDTO {

    private Long id;
    private String plantType;
    private String category;
    private String subCategory;
    private String drawingNo;

    private String vendorCode;
    private String plantId;
    private List<BMDetailResponseDTO> details;

    private Integer createdBy;
    private Date createdDate;
}