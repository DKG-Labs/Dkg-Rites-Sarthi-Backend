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

                private Integer quantityOffered;

                private Integer quantityPassed;

                private Integer quantityRejected;

                private String bkNumber;

                private String setNumber;

                private String icFileLink;

                private LocalDate icDate;


}
