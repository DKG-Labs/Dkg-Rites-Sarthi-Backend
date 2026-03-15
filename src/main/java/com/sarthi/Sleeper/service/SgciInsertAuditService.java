package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.SgciInventory.SgciInsertAuditAuditRequestDto;
import com.sarthi.Sleeper.dto.SgciInventory.SgciInsertAuditAuditResponseDto;
import java.util.List;

public interface SgciInsertAuditService {
    SgciInsertAuditAuditResponseDto create(SgciInsertAuditAuditRequestDto dto);
    SgciInsertAuditAuditResponseDto update(Long id, SgciInsertAuditAuditRequestDto dto);
    SgciInsertAuditAuditResponseDto getById(Long id);
    List<SgciInsertAuditAuditResponseDto> getAll();
    void delete(Long id);
}
