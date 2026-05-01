package com.sarthi.Sleeper.dto.FinalCalDtos;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FinalCallRequestDto {


        private String batchNo;
        private String callNo;
        private String dateCasted;

        private BigDecimal casted;
        private BigDecimal offeredPrev;
        private BigDecimal offeredNow;

        private BigDecimal passed;
        private BigDecimal rejected;

        private BigDecimal totalOffered;
        private BigDecimal totalAccepted;
        private BigDecimal totalRejected;

        private String shift;
        private String plantId;
        private String vendorCode;

        private String createdBy;
        private String updatedBy;

        private List<SleeperDto> goodSleepers;
        private List<RejectedDto> rejectedSleepers;
        private List<SleeperDto> etSleepers;
        private List<SleeperDto> mfSleepers;
        private List<RejectedDto> finalRejections;

}
