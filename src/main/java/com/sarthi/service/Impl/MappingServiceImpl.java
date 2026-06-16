package com.sarthi.service.Impl;

import com.sarthi.dto.IePoiMappingDto;
import com.sarthi.dto.ProcessIeMappingRequestDto;
import com.sarthi.dto.IeMappingResponseDto;
import com.sarthi.entity.PoiProcessIeMapping;
import com.sarthi.entity.UserMaster;
import com.sarthi.entity.IeControllingManager;
import com.sarthi.entity.PincodePoIMapping;
import com.sarthi.repository.PoiProcessIeMappingRepository;
import com.sarthi.repository.UserMasterRepository;
import com.sarthi.repository.UserProductCmMappingRepository;
import com.sarthi.repository.IePincodePoiMappingRepository;
import com.sarthi.repository.IePoiMappingRepository;
import com.sarthi.repository.ieControllingManagerRepository;
import com.sarthi.repository.PincodePoIMappingRepository;
import com.sarthi.service.MappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import java.util.Date;


@Service
public class MappingServiceImpl implements MappingService {

    @Autowired
    private UserMasterRepository userMasterRepository;

    @Autowired
    private PoiProcessIeMappingRepository poiProcessIeMappingRepository;


    @Autowired
    private UserProductCmMappingRepository userProductCmMappingRepository;

    @Autowired
    private IePincodePoiMappingRepository iePincodePoiMappingRepository;

    @Autowired
    private IePoiMappingRepository iePoiMappingRepository;

    @Autowired
    private ieControllingManagerRepository ieControllingManagerRepository;

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

    @Override
    public List<IeMappingResponseDto> getAllMappings() {
        List<IeMappingResponseDto> responseList = new ArrayList<>();

        // Fetch all dependencies to avoid N+1 queries
        List<UserMaster> allUsers = userMasterRepository.findAll();
        Map<Integer, UserMaster> userByIdMap = allUsers.stream()
                .filter(u -> u.getUserId() != null)
                .collect(Collectors.toMap(UserMaster::getUserId, u -> u, (a, b) -> a));
        Map<String, UserMaster> userByEmpCodeMap = allUsers.stream()
                .filter(u -> u.getEmployeeCode() != null && !u.getEmployeeCode().isEmpty())
                .collect(Collectors.toMap(UserMaster::getEmployeeCode, u -> u, (a, b) -> a));

        List<PincodePoIMapping> allPoiMappings = pincodePoIMappingRepository.findAll();
        Map<String, PincodePoIMapping> poiMap = allPoiMappings.stream()
                .filter(p -> p.getPoiCode() != null && !p.getPoiCode().isEmpty())
                .collect(Collectors.toMap(PincodePoIMapping::getPoiCode, p -> p, (a, b) -> a));

        List<IeControllingManager> allCm = ieControllingManagerRepository.findAll();
        Map<String, Integer> ieToCmUserIdMap = allCm.stream()
                .filter(c -> c.getIeEmployeeCode() != null)
                .collect(Collectors.toMap(IeControllingManager::getIeEmployeeCode, IeControllingManager::getCmUserId, (a, b) -> a));

        // 1. UserProductCmMapping (IE to CM)
        List<com.sarthi.entity.UserProductCmMapping> userProductCmMappings = userProductCmMappingRepository.findAll();
        for (com.sarthi.entity.UserProductCmMapping m : userProductCmMappings) {
            IeMappingResponseDto dto = new IeMappingResponseDto();
            dto.setId("cm_" + m.getId());
            dto.setMappingType("IE to CM");
            dto.setStatus("Active");

            UserMaster ieUser = userByEmpCodeMap.get(m.getUserEmployeeCode());
            if (ieUser != null) {
                dto.setIeName(ieUser.getFullName() != null ? ieUser.getFullName() : ieUser.getUsername());
                dto.setRio(ieUser.getRio() != null ? ieUser.getRio() : "-");
            } else {
                dto.setIeName(m.getUserEmployeeCode());
                dto.setRio("-");
            }

            UserMaster cmUser = userByEmpCodeMap.get(m.getCmEmployeeCode());
            if (cmUser != null) {
                dto.setCm(cmUser.getFullName() != null ? cmUser.getFullName() : cmUser.getUsername());
            } else {
                dto.setCm(m.getCmEmployeeCode());
            }

            dto.setPoiCode("-");
            dto.setPoiName("-");
            responseList.add(dto);
        }

        // 2. IePincodePoiMapping (IE to POI)
        List<com.sarthi.entity.IePincodePoiMapping> iePoiMappings = iePincodePoiMappingRepository.findAll();
        for (com.sarthi.entity.IePincodePoiMapping m : iePoiMappings) {
            IeMappingResponseDto dto = new IeMappingResponseDto();
            dto.setId("poi_" + m.getId());
            dto.setMappingType("IE to POI");
            dto.setStatus("Active");
            dto.setPoiCode(m.getPoiCode() != null ? m.getPoiCode() : "-");

            UserMaster ieUser = userByEmpCodeMap.get(m.getEmployeeCode());
            if (ieUser != null) {
                dto.setIeName(ieUser.getFullName() != null ? ieUser.getFullName() : ieUser.getUsername());
                dto.setRio(ieUser.getRio() != null ? ieUser.getRio() : "-");
            } else {
                dto.setIeName(m.getEmployeeCode());
                dto.setRio("-");
            }

            Integer cmUserId = ieToCmUserIdMap.get(m.getEmployeeCode());
            if (cmUserId != null) {
                UserMaster cmUser = userByIdMap.get(cmUserId);
                if (cmUser != null) {
                    dto.setCm(cmUser.getFullName() != null ? cmUser.getFullName() : cmUser.getUsername());
                } else {
                    dto.setCm("CM ID: " + cmUserId);
                }
            } else {
                dto.setCm("-");
            }

            PincodePoIMapping poi = poiMap.get(m.getPoiCode());
            if (poi != null) {
                String name = poi.getCompanyName();
                if (poi.getUnitName() != null && !poi.getUnitName().isEmpty()) {
                    name += " - " + poi.getUnitName();
                }
                dto.setPoiName(name);
            } else {
                dto.setPoiName("-");
            }
            responseList.add(dto);
        }

        // 3. PoiProcessIeMapping (Process IE to POI)
        List<PoiProcessIeMapping> procPoiMappings = poiProcessIeMappingRepository.findAll();
        for (PoiProcessIeMapping m : procPoiMappings) {
            IeMappingResponseDto dto = new IeMappingResponseDto();
            dto.setId("proc_" + m.getId());
            dto.setMappingType("Process IE to POI");
            dto.setStatus("Active");
            dto.setPoiCode(m.getPoiCode() != null ? m.getPoiCode() : "-");

            UserMaster ieUser = userByEmpCodeMap.get(m.getEmployeeCode());
            if (ieUser != null) {
                dto.setIeName(ieUser.getFullName() != null ? ieUser.getFullName() : ieUser.getUsername());
                dto.setRio(ieUser.getRio() != null ? ieUser.getRio() : "-");
            } else {
                dto.setIeName(m.getEmployeeCode());
                dto.setRio("-");
            }

            Integer cmUserId = ieToCmUserIdMap.get(m.getEmployeeCode());
            if (cmUserId != null) {
                UserMaster cmUser = userByIdMap.get(cmUserId);
                if (cmUser != null) {
                    dto.setCm(cmUser.getFullName() != null ? cmUser.getFullName() : cmUser.getUsername());
                } else {
                    dto.setCm("CM ID: " + cmUserId);
                }
            } else {
                dto.setCm("-");
            }

            PincodePoIMapping poi = poiMap.get(m.getPoiCode());
            if (poi != null) {
                String name = poi.getCompanyName();
                if (poi.getUnitName() != null && !poi.getUnitName().isEmpty()) {
                    name += " - " + poi.getUnitName();
                }
                dto.setPoiName(name);
            } else {
                dto.setPoiName("-");
            }
            responseList.add(dto);
        }

        // 4. IePoiMapping (IE to POI via User ID)
        List<com.sarthi.entity.IePoiMapping> iePoiMappingList = iePoiMappingRepository.findAll();
        for (com.sarthi.entity.IePoiMapping m : iePoiMappingList) {
            IeMappingResponseDto dto = new IeMappingResponseDto();
            dto.setId("iepoi_" + m.getId());
            dto.setMappingType("IE to POI");
            dto.setStatus("Active");
            dto.setPoiCode(m.getPoiCode() != null ? m.getPoiCode() : "-");

            UserMaster ieUser = userByIdMap.get(m.getIeUserId() != null ? m.getIeUserId().intValue() : null);
            if (ieUser != null) {
                dto.setIeName(ieUser.getFullName() != null ? ieUser.getFullName() : ieUser.getUsername());
                dto.setRio(ieUser.getRio() != null ? ieUser.getRio() : "-");

                Integer cmUserId = ieToCmUserIdMap.get(ieUser.getEmployeeCode());
                if (cmUserId != null) {
                    UserMaster cmUser = userByIdMap.get(cmUserId);
                    if (cmUser != null) {
                        dto.setCm(cmUser.getFullName() != null ? cmUser.getFullName() : cmUser.getUsername());
                    } else {
                        dto.setCm("CM ID: " + cmUserId);
                    }
                } else {
                    dto.setCm("-");
                }
            } else {
                dto.setIeName("User ID: " + m.getIeUserId());
                dto.setRio("-");
                dto.setCm("-");
            }

            PincodePoIMapping poi = poiMap.get(m.getPoiCode());
            if (poi != null) {
                String name = poi.getCompanyName();
                if (poi.getUnitName() != null && !poi.getUnitName().isEmpty()) {
                    name += " - " + poi.getUnitName();
                }
                dto.setPoiName(name);
            } else {
                dto.setPoiName("-");
            }
            responseList.add(dto);
        }

        return responseList;
    }

    @Transactional
    @Override
    public void deleteMapping(String id) {
        if (id == null || id.isEmpty()) return;

        if (id.startsWith("cm_")) {
            Long rawId = Long.parseLong(id.substring(3));
            userProductCmMappingRepository.deleteById(rawId);
        } else if (id.startsWith("poi_")) {
            Long rawId = Long.parseLong(id.substring(4));
            iePincodePoiMappingRepository.deleteById(rawId);
        } else if (id.startsWith("proc_")) {
            Long rawId = Long.parseLong(id.substring(5));
            poiProcessIeMappingRepository.deleteById(rawId);
        } else if (id.startsWith("iepoi_")) {
            Long rawId = Long.parseLong(id.substring(6));
            iePoiMappingRepository.deleteById(rawId);
        } else {
            try {
                Long rawId = Long.parseLong(id);
                poiProcessIeMappingRepository.deleteById(rawId);
            } catch (NumberFormatException e) {
                // Ignore or log
            }
        }
    }

    private Long safeParseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 1L; // Default to system/admin ID if not a number
        }
    }
}
