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

import com.sarthi.dto.MappingListDto;
import com.sarthi.entity.IePoiMapping;
import com.sarthi.entity.PincodePoIMapping;
import com.sarthi.entity.UserProductCmMapping;
import com.sarthi.repository.IePoiMappingRepository;
import com.sarthi.repository.PincodePoIMappingRepository;
import com.sarthi.repository.UserProductCmMappingRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MappingServiceImpl implements MappingService {

    @Autowired
    private UserMasterRepository userMasterRepository;

    @Autowired
    private PoiProcessIeMappingRepository poiProcessIeMappingRepository;
    
    @Autowired
    private IePoiMappingRepository iePoiMappingRepository;
    
    @Autowired
    private UserProductCmMappingRepository userProductCmMappingRepository;
    
    @Autowired
    private PincodePoIMappingRepository pincodePoIMappingRepository;

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

    @Override
    public List<MappingListDto> getAllMappings() {
        List<UserMaster> allUsers = userMasterRepository.findAll();
        Map<String, UserMaster> userByEmpCode = allUsers.stream()
                .filter(u -> u.getEmployeeCode() != null && !u.getEmployeeCode().trim().isEmpty())
                .collect(Collectors.toMap(UserMaster::getEmployeeCode, u -> u, (u1, u2) -> u1));
                
        Map<Long, UserMaster> userById = allUsers.stream()
                .filter(u -> u.getUserId() != null)
                .collect(Collectors.toMap(u -> u.getUserId().longValue(), u -> u, (u1, u2) -> u1));

        List<PincodePoIMapping> allPois = pincodePoIMappingRepository.findAll();
        Map<String, String> poiNameByCode = allPois.stream()
                .filter(p -> p.getPoiCode() != null && !p.getPoiCode().trim().isEmpty())
                .collect(Collectors.toMap(
                    PincodePoIMapping::getPoiCode, 
                    p -> p.getCompanyName() + (p.getUnitName() != null && !p.getUnitName().isEmpty() ? " - " + p.getUnitName() : ""), 
                    (p1, p2) -> p1
                ));

        List<MappingListDto> result = new ArrayList<>();

        // 1. PoiProcessIeMapping (Process IE to POI)
        List<PoiProcessIeMapping> processIeMappings = poiProcessIeMappingRepository.findAll();
        for (PoiProcessIeMapping p : processIeMappings) {
            MappingListDto dto = new MappingListDto();
            dto.setId("process-" + p.getId());
            dto.setMappingType("Process IE to POI");
            dto.setPoiCode(p.getPoiCode());
            dto.setPoiName(poiNameByCode.getOrDefault(p.getPoiCode(), "Unknown POI"));
            
            UserMaster ie = userByEmpCode.get(p.getEmployeeCode());
            if (ie != null) {
                dto.setIeCode(ie.getEmployeeCode());
                dto.setIeName(ie.getFullName());
                dto.setRio(ie.getRio());
            } else {
                dto.setIeCode(p.getEmployeeCode());
            }
            dto.setStatus("ACTIVE");
            result.add(dto);
        }

        // 2. IePoiMapping (IE to POI)
        List<IePoiMapping> iePoiMappings = iePoiMappingRepository.findAll();
        for (IePoiMapping p : iePoiMappings) {
            MappingListDto dto = new MappingListDto();
            dto.setId("iepoi-" + p.getId());
            dto.setMappingType("IE to POI");
            dto.setPoiCode(p.getPoiCode());
            dto.setPoiName(poiNameByCode.getOrDefault(p.getPoiCode(), "Unknown POI"));
            
            UserMaster ie = userById.get(p.getIeUserId());
            if (ie != null) {
                dto.setIeCode(ie.getEmployeeCode());
                dto.setIeName(ie.getFullName());
                dto.setRio(ie.getRio());
            }
            dto.setStatus("ACTIVE");
            result.add(dto);
        }

        // 3. UserProductCmMapping (IE to CM)
        List<UserProductCmMapping> ieCmMappings = userProductCmMappingRepository.findAll();
        for (UserProductCmMapping p : ieCmMappings) {
            MappingListDto dto = new MappingListDto();
            dto.setId("cm-" + p.getId());
            dto.setMappingType("IE to CM");
            
            UserMaster ie = userByEmpCode.get(p.getUserEmployeeCode());
            if (ie != null) {
                dto.setIeCode(ie.getEmployeeCode());
                dto.setIeName(ie.getFullName());
                dto.setRio(ie.getRio());
            } else {
                dto.setIeCode(p.getUserEmployeeCode());
            }
            
            UserMaster cm = userByEmpCode.get(p.getCmEmployeeCode());
            if (cm != null) {
                dto.setCm(cm.getFullName());
            } else {
                dto.setCm(p.getCmEmployeeCode());
            }
            dto.setStatus("ACTIVE");
            result.add(dto);
        }

        return result;
    }

    @Transactional
    @Override
    public void deleteMappingById(String id) {
        if (id == null) return;
        
        if (id.startsWith("process-")) {
            Long realId = Long.parseLong(id.substring("process-".length()));
            poiProcessIeMappingRepository.deleteById(realId);
        } else if (id.startsWith("iepoi-")) {
            Long realId = Long.parseLong(id.substring("iepoi-".length()));
            iePoiMappingRepository.deleteById(realId);
        } else if (id.startsWith("cm-")) {
            Long realId = Long.parseLong(id.substring("cm-".length()));
            userProductCmMappingRepository.deleteById(realId);
        } else {
            throw new IllegalArgumentException("Invalid mapping ID format: " + id);
        }
    }
}
