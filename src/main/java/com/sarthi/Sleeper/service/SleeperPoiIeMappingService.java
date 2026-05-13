package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.CompanyUnitResponseDto;
import com.sarthi.Sleeper.dto.SleeperPoiIeMappingDto;
import com.sarthi.Sleeper.entity.SleeperPoiIeMapping;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SleeperPoiIeMappingService {

    public List<SleeperPoiIeMapping> saveMapping(SleeperPoiIeMappingDto dto);

    public List<CompanyUnitResponseDto> getCompanyUnits(Integer ieUserId);
}
