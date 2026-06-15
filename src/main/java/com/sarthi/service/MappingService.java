package com.sarthi.service;

import com.sarthi.dto.ProcessIeMappingRequestDto;

import com.sarthi.dto.IeMappingResponseDto;
import java.util.List;

public interface MappingService {
    
    Object mapProcessIe(Long userId, ProcessIeMappingRequestDto dto, String createdBy);
    
    List<IeMappingResponseDto> getAllMappings();
    
    void deleteMapping(String id);
}
