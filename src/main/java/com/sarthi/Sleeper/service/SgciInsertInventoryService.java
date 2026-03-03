package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.SgciInventory.SgciInsertRequestDto;
import com.sarthi.Sleeper.dto.SgciInventory.SgciInsertResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SgciInsertInventoryService {

     public SgciInsertResponseDto create(SgciInsertRequestDto dto);

     public SgciInsertResponseDto update(Long id, SgciInsertRequestDto dto);

     public SgciInsertResponseDto getById(Long id);

     public List<SgciInsertResponseDto> getAll();

     public void delete(Long id);

}
