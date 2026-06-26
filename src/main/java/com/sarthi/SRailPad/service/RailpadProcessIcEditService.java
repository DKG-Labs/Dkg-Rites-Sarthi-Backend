package com.sarthi.SRailPad.service;

import com.sarthi.SRailPad.dto.RailpadProcessIcEditDTO;

public interface RailpadProcessIcEditService {
    RailpadProcessIcEditDTO getByIcNumber(String icNumber);
    RailpadProcessIcEditDTO saveOrUpdate(RailpadProcessIcEditDTO dto);
}
