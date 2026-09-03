package com.sarthi.Sleeper.dto.SleeperDashboardDtos;


import java.time.LocalDate;
import java.time.LocalDateTime;

public interface SleeperIcProjection {

    String getCertificateNo();

    String getBookNo();

    String getSetNo();

    String getDate();

    Integer getOfferedInstallmentNumber();

    Integer getPassedInstallmentNumber();

    String getContractor();

    String getPlaceOfInspection();

    String getContractRefAndDate();

    String getBillPayingOffice();

    String getConsignee();

    String getPurchasingAuthority();

    String getItemNo();

    String getDescriptionOfStores();

    String getQuantityOnOrder();

    String getCumulativeQtyOfferedPreviously();

    String getQuantityPreviouslyPassed();

    String getQtyNowOffered();

    String getQtyNowPassed();

    String getQtyNowRejected();

    String getQtyStillDue();

    LocalDate getDateOfCall();

    Integer getNoOfVisits();

    LocalDateTime getDateOfInspection();

    String getQuantityNowPassedBatchNos();
}
