package com.sarthi.service;

import com.sarthi.dto.ProcessIeMappingRequestDto;

public interface MappingService {
    
    Object mapProcessIe(Long userId, ProcessIeMappingRequestDto dto, String createdBy);
    
}
