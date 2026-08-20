package com.sarthi.SRailPad.service.inspectionCall;

import com.sarthi.SRailPad.dto.RailProcessCallDto;
import com.sarthi.SRailPad.dto.RailProcessCallUpdateDto;

public interface RailProcessCallService {
    RailProcessCallDto getProcessCallDetails(String callNo);
    RailProcessCallDto updateProcessCallDetails(String callNo, RailProcessCallUpdateDto updateDto);
    com.sarthi.SRailPad.dto.inspectionCall.ProcessAvailableBatchDto getAvailableBatchesForProcessIc(String poNo, String railPadType, String callNo);
    void saveProcessInspectionResult(com.sarthi.SRailPad.dto.inspectionCall.ProcessInspectionSaveDto saveDto);
    com.sarthi.SRailPad.dto.inspectionCall.ProcessInspectionSaveDto getProcessInspectionResult(String callNo);
    com.sarthi.SRailPad.dto.inspectionCall.ProcessInspectionSaveDto getAvailableBatchesForFinalCall(String callNo);
    com.sarthi.SRailPad.dto.inspectionCall.ProcessInspectionSaveDto getAvailableBatchesForFinalCall(String callNo, String excludeCallNo);
}
