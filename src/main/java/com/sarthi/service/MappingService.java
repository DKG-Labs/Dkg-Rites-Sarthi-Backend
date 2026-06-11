package com.sarthi.service;

import com.sarthi.dto.ProcessIeMappingRequestDto;
import com.sarthi.dto.MappingListDto;
import java.util.List;

public interface MappingService {
    
    Object mapProcessIe(Long userId, ProcessIeMappingRequestDto dto, String createdBy);

    List<MappingListDto> getAllMappings();
    
    void deleteMappingById(String id);
}
