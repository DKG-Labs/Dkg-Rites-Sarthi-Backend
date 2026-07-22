package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailRheometerTestDto;

import java.util.List;

public interface RailRheometerTestService {

    RailRheometerTestDto createRheometerTest(RailRheometerTestDto dto);

    RailRheometerTestDto updateRheometerTest(Long id, RailRheometerTestDto dto);

    void deleteRheometerTest(Long id);

    RailRheometerTestDto getRheometerTestById(Long id);

    List<RailRheometerTestDto> getRheometerTestByPlantAndVendor(String plantId, String vendorCode);
}
