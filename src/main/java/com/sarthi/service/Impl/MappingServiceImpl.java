package com.sarthi.service.Impl;

import com.sarthi.dto.IePoiMappingDto;
import com.sarthi.dto.ProcessIeMappingRequestDto;
import com.sarthi.entity.PoiProcessIeMapping;
import com.sarthi.entity.UserMaster;
import com.sarthi.repository.PoiProcessIeMappingRepository;
import com.sarthi.repository.UserMasterRepository;
import com.sarthi.service.MappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class MappingServiceImpl implements MappingService {

    @Autowired
    private UserMasterRepository userMasterRepository;

    @Autowired
    private PoiProcessIeMappingRepository poiProcessIeMappingRepository;

    @Transactional
    @Override
    public Object mapProcessIe(Long userId, ProcessIeMappingRequestDto dto, String createdBy) {

        UserMaster processIeUser = userMasterRepository.findById(userId.intValue())
                .orElseThrow(() -> new RuntimeException("Process IE user not found"));
        
        String employeeCode = processIeUser.getEmployeeCode();

        for (IePoiMappingDto ieDto : dto.getIePoiMappings()) {
            for (String poi : ieDto.getPoiCodes()) {
                
                // Process IE EmployeeCode -> POI mapping
                PoiProcessIeMapping poiProcessMap = new PoiProcessIeMapping();
                poiProcessMap.setEmployeeCode(employeeCode);
                poiProcessMap.setPoiCode(poi);
                poiProcessMap.setCreatedBy(safeParseLong(createdBy));
                poiProcessMap.setCreatedDate(new Date());
                poiProcessIeMappingRepository.save(poiProcessMap);
            }
        }
        return null;
    }

    private Long safeParseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 1L; // Default to system/admin ID if not a number
        }
    }
}
