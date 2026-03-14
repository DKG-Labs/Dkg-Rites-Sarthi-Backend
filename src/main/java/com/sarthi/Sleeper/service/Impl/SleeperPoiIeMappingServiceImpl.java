package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.CompanyUnitResponseDto;
import com.sarthi.Sleeper.dto.SleeperPoiIeMappingDto;
import com.sarthi.Sleeper.entity.SleeperPincodePoIMapping;
import com.sarthi.Sleeper.entity.SleeperPoiIeMapping;
import com.sarthi.Sleeper.repository.SleeperPincodePoIMappingRepository;
import com.sarthi.Sleeper.repository.SleeperPoiIeMappingRepository;
import com.sarthi.Sleeper.service.SleeperPoiIeMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SleeperPoiIeMappingServiceImpl implements SleeperPoiIeMappingService {

    @Autowired
    private SleeperPoiIeMappingRepository poiIeMappingRepository;
    @Autowired
    private SleeperPincodePoIMappingRepository sleeperPincodePoIMappingRepository;
    @Override
    public List<SleeperPoiIeMapping> saveMapping(SleeperPoiIeMappingDto dto) {

        List<SleeperPoiIeMapping> savedList = new ArrayList<>();

        for (Integer userId : dto.getIeUserIds()) {

            SleeperPoiIeMapping entity = new SleeperPoiIeMapping();

            entity.setPoiCode(dto.getPoiCode());
            entity.setIeUserId(userId);
            entity.setIeType(dto.getIeType());

            savedList.add(poiIeMappingRepository.save(entity));
        }

        return savedList;
    }

    public CompanyUnitResponseDto getCompanyUnits(Integer ieUserId) {

        // Step 1: Get POI codes from IE mapping
        List<String> poiCodes = poiIeMappingRepository
                .findByIeUserId(ieUserId)
                .stream()
                .map(SleeperPoiIeMapping::getPoiCode)
                .toList();

        // Step 2: Get company and unit names
        List<SleeperPincodePoIMapping> mappings =
               sleeperPincodePoIMappingRepository.findByPoiCodeIn(poiCodes);

        if (mappings.isEmpty()) {
            throw new RuntimeException("No company found for IE " + ieUserId);
        }

        CompanyUnitResponseDto dto = new CompanyUnitResponseDto();

        dto.setCompanyName(mappings.get(0).getCompanyName());

        dto.setUnitNames(
                mappings.stream()
                        .map(SleeperPincodePoIMapping::getUnitName)
                        .distinct()
                        .toList()
        );

        return dto;
    }


}
