package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailMouldVerificationDto;

import java.util.List;

public interface RailMouldVerificationService {

    RailMouldVerificationDto createMouldVerification(RailMouldVerificationDto dto);

    RailMouldVerificationDto updateMouldVerification(Long id, RailMouldVerificationDto dto);

    void deleteMouldVerification(Long id);

    List<RailMouldVerificationDto> getMouldVerifications(String plantId, String vendorCode);
}
