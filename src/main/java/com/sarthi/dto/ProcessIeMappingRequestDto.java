package com.sarthi.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProcessIeMappingRequestDto {

    // id of the poi_process_ie_mapping row being edited; null when creating new mappings
    private Long id;

    private List<IePoiMappingDto> iePoiMappings;
}
