package com.sarthi.dto.IBS;

import lombok.Data;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
public class IbsInspectionDto {



                private String caseNumber;

                // Better to use LocalDate
                private LocalDate callDate;

                private String placeOfInspection;
                private String ibsManufacturedCode;

                private String ieEmployeeNumber;

                private String callStatus;

                private String typeOfCall;

                private List<String> poItemSerialNumbers;

                private Double quantityOffered;

                private Double quantityPassed;

                private Double quantityRejected;

                private String bkNumber;

                private String setNumber;

                private String icFileLink;

                private LocalDate icDate;
                private String callNumber;
                private String icNumber;

    @com.fasterxml.jackson.annotation.JsonProperty("is_blocked")
    private Integer isBlocked;

    @com.fasterxml.jackson.annotation.JsonProperty("cancellation_charges")
    private Double cancellationCharges;

    @com.fasterxml.jackson.annotation.JsonProperty("rejection_charges")
    private Double rejectionCharges;

    private String srNo;
    private String ibsStatus;
    private String reason;
    private Integer version;
    private String billingStatus;
    private java.time.LocalDateTime acknowledgedAt;
}
