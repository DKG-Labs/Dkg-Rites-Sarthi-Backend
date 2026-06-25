package com.sarthi.SRailPad.service;

import com.sarthi.SRailPad.dto.RailpadFinalIcEditDTO;

public interface RailpadFinalIcSaveChangesService {
    RailpadFinalIcEditDTO getByIcNumber(String icNumber);
    RailpadFinalIcEditDTO saveOrUpdate(RailpadFinalIcEditDTO dto);
}
