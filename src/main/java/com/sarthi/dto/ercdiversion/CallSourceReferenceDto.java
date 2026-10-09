package com.sarthi.dto.ercdiversion;

import com.sarthi.dto.rawmaterial.RmChemicalAnalysisDto;
import com.sarthi.dto.rawmaterial.RmHeatQuantityDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallSourceReferenceDto {
    private String diversionRequestNo;
    private String sourceIcNo;
    private String sourceCallNo;
    private LocalDate sourceIcDate;
    private String sourcePoNo;
    private String sourcePoSrNo;
    private String stage;

    private List<BasketItemDto> allocatedItems;
    private List<RmHeatQuantityDto> sourceHeatQuantities;
    private List<RmChemicalAnalysisDto> sourceChemicalAnalyses;
    private String sourceIcDocumentUrl;
}
