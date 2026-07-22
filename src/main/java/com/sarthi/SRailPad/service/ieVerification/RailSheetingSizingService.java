package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailSheetingSizingDto;

import java.util.List;

public interface RailSheetingSizingService {
    
    RailSheetingSizingDto createSheetingSizing(RailSheetingSizingDto dto);
    
    RailSheetingSizingDto updateSheetingSizing(Long id, RailSheetingSizingDto dto);
    
    void deleteSheetingSizing(Long id);
    
    RailSheetingSizingDto getSheetingSizingById(Long id);
    
    List<RailSheetingSizingDto> getSheetingSizingByPlantAndVendor(String plantId, String vendorCode);
}
