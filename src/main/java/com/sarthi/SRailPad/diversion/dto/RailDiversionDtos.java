package com.sarthi.SRailPad.diversion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** DTOs for Rail Pad material diversion. */
public final class RailDiversionDtos {

    private RailDiversionDtos() {}

    /** Vendor's raise / edit request. Vendor code is taken from the login, not from here. */
    @Data
    public static class RequestDto {
        private Long id;
        private String plantId;
        private String plantName;
        private String plantAddress;
        private String companyName;
        private String rioId;
        private String cmUserId;
        private String stage;            // PROCESS / FINAL
        private String diversionType;    // PARTIAL / COMPLETE
        private String sourceIcNo;       // certificate no. or call no. of the source IC
        private String targetPoNo;
        private String targetPoSrNo;
        private String unitOfMeasurement;
        private String reasonCode;
        private String reasonRemarks;
        private String railwayPermissionNo;
        private LocalDate railwayPermissionDate;
        private String railwayPermissionDocUrl;
        private String vendorRemarks;
        private List<ItemDto> items = new ArrayList<>();
    }

    @Data
    public static class ItemDto {
        private String lotNo;
        private String batchNo;
        private BigDecimal diversionQty;
    }

    /** One eligible source IC. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceIcDto {
        private String callNo;
        private String icNo;
        private String poNo;
        private String poSrNo;
        private String plantId;
        private LocalDate icDate;
        private String ieUserId;
        private String railPadType;
        private BigDecimal acceptedQty;
        private BigDecimal alreadyDivertedQty;
        private BigDecimal availableQty;
    }

    /** One lot / batch line of a source IC with its balance. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceLineDto {
        private String lotNo;
        private String batchNo;
        private BigDecimal acceptedQty;
        private BigDecimal downstreamConsumedQty;
        private BigDecimal alreadyDivertedQty;
        private BigDecimal availableQty;
    }

    /** CM / SBU action. Acting user is taken from the login. */
    @Data
    public static class ActionDto {
        private Long requestId;
        private String remarks;
        private Long assignedIeUserId;   // required for approve (IE who issues the diverted IC)
    }

    /** Diverted IC available for a DIVERTED call on the target PO. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DivertedIcBalanceDto {
        private String divertedCallNo;
        private String icNo;
        private String requestNo;
        private String stage;
        private String sourceIcNo;
        private BigDecimal approvedQty;
        private BigDecimal allocatedQty;
        private BigDecimal consumedQty;
        private BigDecimal availableQty;
    }
}
