package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailVisualInspectionDto;
import java.util.List;

public interface RailVisualInspectionService {
    RailVisualInspectionDto create(RailVisualInspectionDto dto);
    RailVisualInspectionDto update(Long id, RailVisualInspectionDto dto);
    List<RailVisualInspectionDto> getList(String plantId, String vendorCode);
    void delete(Long id);
}
