package com.sarthi.SRailPad.service.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall;
import java.util.List;

public interface RailInspectionCallService {
    RailInspectionCall create(RailInspectionCall call);
    List<RailInspectionCall> getAllByVendorCode(String vendorCode);
    RailInspectionCall getById(Long id);
    RailInspectionCall getByCallNo(String callNo);
}
