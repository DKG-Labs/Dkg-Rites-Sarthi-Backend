package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.SleeperPoiIeMappingDto;
import com.sarthi.Sleeper.entity.SleeperPoiIeMapping;
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
}
