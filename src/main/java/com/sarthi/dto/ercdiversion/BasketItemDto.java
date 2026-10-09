package com.sarthi.dto.ercdiversion;

import com.sarthi.enums.BasketStatus;
import com.sarthi.enums.DiversionStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasketItemDto {
    private Long id;
    private Long diversionRequestId;
    private String diversionRequestNo;
    private DiversionStage stage;
    private String vendorCode;
    private Long plantId;
    private String rioId;

    private String sourcePoNo;
    private String sourcePoSrNo;
    private String sourceIcNo;

    private String targetPoNo;
    private String targetPoSrNo;

    private String heatNo;
    private String tcNo;
    private String lotNo;

    private BigDecimal approvedDivertedQty;
    private BigDecimal allocatedCallQty;
    private BigDecimal consumedIcQty;
    private BigDecimal availableBalanceQty;
    private BasketStatus status;
}
