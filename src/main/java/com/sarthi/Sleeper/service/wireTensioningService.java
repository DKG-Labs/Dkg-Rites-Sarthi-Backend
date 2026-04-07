package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.WireTensioningDtos.WireTensioningRequestDto;
import com.sarthi.Sleeper.dto.WireTensioningDtos.WireTensioningResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface wireTensioningService {

    WireTensioningResponseDto create(WireTensioningRequestDto dto);

    WireTensioningResponseDto update(Long id, WireTensioningRequestDto dto);

    WireTensioningResponseDto getById(Long id);

    List<WireTensioningResponseDto> getAll();

    void delete(Long id);

    public List<WireTensioningResponseDto> getRecordsByDate(
            String plantId,
            String vendorCode,
            String shift,
            int createdBy,
            String date);
}
