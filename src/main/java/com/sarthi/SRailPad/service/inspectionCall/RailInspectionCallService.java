package com.sarthi.SRailPad.service.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RailInspectionCallService {
    RailInspectionCall create(RailInspectionCall call);
    
    Page<RailInspectionCall> getPaginatedCallsByVendor(String vendorCode, Pageable pageable);
    
    Page<RailInspectionCall> getPaginatedCallsByPlant(String plantId, String statusType, Pageable pageable);
    
    Page<RailInspectionCall> getCompletedPaginatedCallsByPlant(String plantId, Pageable pageable);

    List<RailInspectionCall> getAllByVendorCode(String vendorCode);

    List<RailInspectionCall> getAllByPlantId(String plantId);
    RailInspectionCall getById(Long id);
    RailInspectionCall getByCallNo(String callNo);
    com.sarthi.SRailPad.dto.RailpadIcCertificateDto getRailpadIcDetails(String callNo);
    List<RailInspectionCall> getProcessCallsByTypeDrawingAndPlant(String railPadType, String drawingNo, String plantId);
    List<RailInspectionCall> getProcessCalls(String railPadType, String drawingNo, String plantId, String poNo, String poSr);
    RailInspectionCall modifyCall(com.sarthi.SRailPad.dto.RailCallModificationDto dto);
    String withdrawCall(com.sarthi.SRailPad.dto.RailWithdrawRequestDto dto);
}
