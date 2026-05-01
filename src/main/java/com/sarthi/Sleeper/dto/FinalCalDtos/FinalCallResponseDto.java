package com.sarthi.Sleeper.dto.FinalCalDtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class FinalCallResponseDto {


        private Long id;
        private String batchNo;
        private String callNo;
        private LocalDate dateCasted;

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

        private List<SleeperDto> goodSleepers;
        private List<SleeperDto> etSleepers;
        private List<SleeperDto> mfSleepers;
        private List<RejectedDto> rejectedSleepers;
        private List<RejectedDto> finalRejections;

}
