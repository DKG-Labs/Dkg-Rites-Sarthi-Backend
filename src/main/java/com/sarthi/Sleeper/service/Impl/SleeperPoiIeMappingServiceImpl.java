package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.CompanyUnitResponseDto;
import com.sarthi.Sleeper.dto.SleeperPoiIeMappingDto;
import com.sarthi.Sleeper.entity.SleeperPincodePoIMapping;
import com.sarthi.Sleeper.entity.SleeperPoiIeMapping;
import com.sarthi.Sleeper.entity.VendorPlant;
import com.sarthi.Sleeper.repository.PlantProfileRepository;
import com.sarthi.Sleeper.repository.SleeperPincodePoIMappingRepository;
import com.sarthi.Sleeper.repository.SleeperPoiIeMappingRepository;
import com.sarthi.Sleeper.repository.VendorPlantRepository;
import com.sarthi.Sleeper.service.SleeperPoiIeMappingService;
import com.sarthi.entity.UserMaster;
import com.sarthi.repository.UserMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SleeperPoiIeMappingServiceImpl implements SleeperPoiIeMappingService {

    @Autowired
    private SleeperPoiIeMappingRepository poiIeMappingRepository;
    @Autowired
    private SleeperPincodePoIMappingRepository sleeperPincodePoIMappingRepository;
    @Autowired
    private UserMasterRepository userMasterRepository;
    @Autowired
    private VendorPlantRepository vendorPlantRepository;
    @Autowired
    private PlantProfileRepository plantProfileRepository;
    @Override
    public List<SleeperPoiIeMapping> saveMapping(SleeperPoiIeMappingDto dto) {

        List<SleeperPoiIeMapping> savedList = new ArrayList<>();

        for (Integer userId : dto.getIeUserIds()) {

            SleeperPoiIeMapping entity = new SleeperPoiIeMapping();

            entity.setPoiCode(dto.getPoiCode());
            entity.setIeUserId(userId);
            entity.setIeType(dto.getIeType());

            entity.setPlantId(dto.getPlantId());
            savedList.add(poiIeMappingRepository.save(entity));
        }

        return savedList;
    }

    /*

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

        dto.setVendorId(mappings.get(0).getVendorCode());

        Optional<UserMaster> userMaster = userMasterRepository.findByUserId(Integer.valueOf(mappings.get(0).getVendorCode()));

        if(userMaster.isPresent()){
            UserMaster um = userMaster.get();
            dto.setVendorCode(um.getUsername());
        }
        dto.setCompanyNames(
                mappings.stream()
                        .map(SleeperPincodePoIMapping::getCompanyName)
                        .distinct()
                        .toList()
        );

        dto.setUnitNames(
                mappings.stream()
                        .map(SleeperPincodePoIMapping::getUnitName)
                        .distinct()
                        .toList()
        );

        java.util.Map<String, String> unitVendorMap = new java.util.HashMap<>();
        java.util.Map<String, List<String>> companyUnitMap = new java.util.HashMap<>();
        for (SleeperPincodePoIMapping mapping : mappings) {
            if (mapping.getCompanyName() != null) {
                if (mapping.getUnitName() != null) {
                    companyUnitMap.computeIfAbsent(mapping.getCompanyName(), k -> new java.util.ArrayList<>())
                                  .add(mapping.getUnitName());
                    
                    if (mapping.getVendorCode() != null) {
                        unitVendorMap.put(mapping.getUnitName(), mapping.getVendorCode());
                    }
                }
            }
        }
        
        // Remove duplicate unit names within each company
        companyUnitMap.replaceAll((k, v) -> v.stream().distinct().toList());

        dto.setUnitVendorMap(unitVendorMap);
        dto.setCompanyUnitMap(companyUnitMap);

        return dto;
    }
*/
    /*
    public CompanyUnitResponseDto getCompanyUnits(Integer ieUserId) {

        // Step 1: Get POI codes
        List<String> poiCodes = poiIeMappingRepository
                .findByIeUserId(ieUserId)
                .stream()
                .map(SleeperPoiIeMapping::getPoiCode)
                .toList();

        // Step 2: Get mappings
        List<SleeperPincodePoIMapping> mappings =
                sleeperPincodePoIMappingRepository.findByPoiCodeIn(poiCodes);

        if (mappings.isEmpty()) {
            throw new RuntimeException("No company found for IE " + ieUserId);
        }

        CompanyUnitResponseDto dto = new CompanyUnitResponseDto();

        String vendorCode = mappings.get(0).getVendorCode();

        dto.setCompanyName(mappings.get(0).getCompanyName());
        dto.setVendorId(vendorCode);

        // Optional: username
        userMasterRepository.findByUserId(Integer.valueOf(vendorCode))
                .ifPresent(um -> dto.setVendorCode(um.getUsername()));

        // Step 3: Fetch plants from vendor_plant
       // List<VendorPlant> plants = vendorPlantRepository.findByVendorId(vendorCode);

        Long vendorId = Long.valueOf(vendorCode);

        List<VendorPlant> plants = vendorPlantRepository.findByVendorId(vendorId);
        List<String> plantIds = plants.stream()
                .map(VendorPlant::getPlantId)
                .distinct()
                .toList();

        dto.setUnitNames(plantIds);
/*
        // Step 4: Fetch lines/sheds from Plant Declaration
        Map<String, List<String>> plantLineMap = new HashMap<>();

        for (String plantId : plantIds) {

            List<String> lines = plantProfileRepository
                    .findLinesByVendorCodeAndPlantId(vendorCode, plantId);

            plantLineMap.put(plantId, lines);
        }

        dto.setCompanyUnitMap(plantLineMap);

        Map<String, String> plantVendorMap = new HashMap<>();
        for (String plantId : plantIds) {
            plantVendorMap.put(plantId, vendorCode);
        }*/

       /* dto.setCompanyUnitMap(null);
        dto.setUnitVendorMap(null);

        return dto;
    } */
       public CompanyUnitResponseDto getCompanyUnits(Integer ieUserId) {

           List<SleeperPoiIeMapping> mappingList =
                   poiIeMappingRepository.findByIeUserId(ieUserId);

           if (mappingList.isEmpty()) {
               throw new RuntimeException("No mapping found for IE " + ieUserId);
           }

           List<String> poiCodes = mappingList.stream()
                   .map(SleeperPoiIeMapping::getPoiCode)
                   .distinct()
                   .toList();

           List<String> userPlantIds = mappingList.stream()
                   .map(SleeperPoiIeMapping::getPlantId)
                   .filter(Objects::nonNull)
                   .distinct()
                   .toList();


           // STEP 2: Get company mapping using POI
           List<SleeperPincodePoIMapping> mappings =
                   sleeperPincodePoIMappingRepository.findByPoiCodeIn(poiCodes);

           if (mappings.isEmpty()) {
               throw new RuntimeException("No company found for IE " + ieUserId);
           }

           CompanyUnitResponseDto dto = new CompanyUnitResponseDto();

           String vendorCode = mappings.get(0).getVendorCode();

           dto.setCompanyName(mappings.get(0).getCompanyName());
           dto.setVendorId(vendorCode);

           userMasterRepository.findByUserId(Integer.valueOf(vendorCode))
                   .ifPresent(um -> dto.setVendorCode(um.getUsername()));


           Long vendorId = Long.valueOf(vendorCode);

           List<VendorPlant> plants = vendorPlantRepository.findByVendorId(vendorId);

           List<String> plantIds = plants.stream()
                   .map(VendorPlant::getPlantId)
                   .filter(userPlantIds::contains)
                   .distinct()
                   .toList();


           if (plantIds.isEmpty()) {
               throw new RuntimeException("No plants mapped for this user");
           }

           dto.setUnitNames(plantIds);


           // (Optional future logic remains same)
           dto.setCompanyUnitMap(null);
           dto.setUnitVendorMap(null);

           return dto;
       }

}
