package com.sarthi.SRailPad.service.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.IEProductionVerificationRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.IEProductionVerificationResponseDto;

import java.util.List;

public interface RailIEProductionVerificationService {
    IEProductionVerificationResponseDto create(IEProductionVerificationRequestDto requestDto);
    IEProductionVerificationResponseDto getById(Long id);
    IEProductionVerificationResponseDto getByRequestId(Long requestId);
    List<IEProductionVerificationResponseDto> getAll();
    void deleteByRequestId(Long requestId);
    List<com.sarthi.SRailPad.dto.ieVerification.RailAcceptedInventoryDto> getAcceptedInventory(String productionUnit, String productType);
}
