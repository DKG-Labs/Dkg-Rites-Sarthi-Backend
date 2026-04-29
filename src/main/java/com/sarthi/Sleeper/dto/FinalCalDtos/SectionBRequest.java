package com.sarthi.Sleeper.dto.FinalCalDtos;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SectionBRequest {

    private String callNo;

    private LocalDateTime inspectionCallDate;
    private LocalDate inspectionDesiredDate;

    private String rlyPoSr;
    private String itemDesc;

    private String productType;
    private String typeOfErc;

    private String poSrQtyUnit;
    private String consignee;

    private LocalDateTime origDp;
    private LocalDateTime extDp;
    private LocalDate origDpStart;

    private String stageOfInspection;
    private Integer callQtyMt;

    private String placeOfInspection;

    private String processIcNumbers;
    private String remarks;

    private String plantId;
    private String vendorCode;
    private String shift;

    private Long createdBy;
}
