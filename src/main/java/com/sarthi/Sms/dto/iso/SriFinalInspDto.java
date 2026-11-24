package com.sarthi.Sms.dto.iso;

import java.util.List;

import lombok.Data;

@Data
public class SriFinalInspDto {
    ShortRailMasterDto masterDtls;
    List<ShortRailRejectionAnalysisDto> rejectionAnalysisDtls;
}
