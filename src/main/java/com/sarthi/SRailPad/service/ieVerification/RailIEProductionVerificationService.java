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
    void unblockProductionVerification(Long requestId);
    java.util.Map<String, Object> deleteVerifiedProductionByCriteria(String dateStr, String shift, String productionLine, String poNo);
    List<com.sarthi.SRailPad.dto.ieVerification.RailAcceptedInventoryDto> getAcceptedInventory(String productionUnit, String productType);
}
