package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SleeperIcResponseDto {

    private String certificateNo;
    private String bookNo;
    private String setNo;
    private LocalDate date;

    private Integer offeredInstallmentNumber;
    private Integer passedInstallmentNumber;

    private String contractor;
    private String placeOfInspection;

    private String contractRefAndDate;

    private String billPayingOffice;

    private String consignee;

    private String purchasingAuthority;

    private String itemNo;

    private String descriptionOfStores;

    private  String getQuantityOnOrder;

    private String getCumulativeQtyOfferedPreviously;

   private String getQuantityPreviouslyPassed;

    private String getQtyNowOffered;

    private String getQtyNowPassed;

   private String getQtyNowRejected;

   private String getQtyStillDue;

   private LocalDate getDateOfCall;

   private  Integer getNoOfVisits;

    private LocalDateTime getDateOfInspection;

    private String getQuantityNowPassedBatchNos;
}