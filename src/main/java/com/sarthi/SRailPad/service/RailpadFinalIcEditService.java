package com.sarthi.SRailPad.service;

import com.sarthi.SRailPad.dto.RailpadFinalIcEditDTO;

public interface RailpadFinalIcEditService {
    RailpadFinalIcEditDTO getByIcNumber(String icNumber);
    RailpadFinalIcEditDTO saveOrUpdate(RailpadFinalIcEditDTO dto);
}
