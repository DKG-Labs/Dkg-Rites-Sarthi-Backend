package com.sarthi.dto.ercdiversion;

import com.sarthi.enums.DiversionRequestStatus;
import com.sarthi.enums.DiversionStage;
import com.sarthi.enums.DiversionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiversionRequestDto {
    private Long id;
    private String requestNo;
    private String vendorCode;
    private Long companyId;
    private String companyName;
    private Long plantId;
    private String plantName;
    private String plantAddress;
    private String rioId;
    private String cmUserId;
    private String cmUserName;
    private String sbuHeadUserId;
    private DiversionStage stage;
    private DiversionType diversionType;

    private String sourcePoNo;
    private String sourcePoSrNo;
    private String sourceIcNo;
    private String sourceCallNo;
    private LocalDate sourceIcDate;

    private String targetPoNo;
    private String targetPoSrNo;
    private BigDecimal totalDiversionQty;
    private String unitOfMeasurement;

    private String reasonCode;
    private String reasonRemarks;
    private String railwayPermissionNo;
    private LocalDate railwayPermissionDate;
    private String railwayPermissionDocUrl;
    private String vendorRemarks;

    private DiversionRequestStatus status;
    private LocalDateTime createdDate;
    private String createdBy;

    private List<DiversionItemDto> items;
}
