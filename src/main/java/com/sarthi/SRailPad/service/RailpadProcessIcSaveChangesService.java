package com.sarthi.SRailPad.service;

import com.sarthi.SRailPad.dto.RailpadProcessIcEditDTO;

public interface RailpadProcessIcSaveChangesService {
    RailpadProcessIcEditDTO getByIcNumber(String icNumber);
    RailpadProcessIcEditDTO saveOrUpdate(RailpadProcessIcEditDTO dto);
}
