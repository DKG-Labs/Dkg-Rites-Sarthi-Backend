package com.sarthi.Sleeper.dto.FinalInspectionDtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@NoArgsConstructor
public class BatchTestingListResponseDto {

    private Long batchId;
    private String batchNumber;
    private String sleeperType;
    private Integer totalBatchQty;
    private Long noOfSleepers;
    private Double testedPercentage;
    private String testingStatus;
    private LocalDate testingDate;

    public BatchTestingListResponseDto(Long batchId,
                                       String batchNumber,
                                       String sleeperType,
                                       Integer totalBatchQty,
                                       Long noOfSleepers,
                                       Double testedPercentage,
                                       String testingStatus,
                                       LocalDate testingDate) {

        this.batchId = batchId;
        this.batchNumber = batchNumber;
        this.sleeperType = sleeperType;
        this.totalBatchQty = totalBatchQty;
        this.noOfSleepers = noOfSleepers;
        this.testedPercentage = testedPercentage;
        this.testingStatus = testingStatus;
        this.testingDate = testingDate;
    }

}