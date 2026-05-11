package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.mapping.*;
import com.sarthi.Sleeper.entity.SleeperPincodePoIMapping;
import com.sarthi.Sleeper.entity.SleeperPoiIeMapping;
import com.sarthi.Sleeper.repository.SleeperPincodePoIMappingRepository;
import com.sarthi.Sleeper.repository.SleeperPoiIeMappingRepository;
import com.sarthi.Sleeper.repository.VendorPlantRepository;
import com.sarthi.Sleeper.service.mappingService;
import com.sarthi.entity.UserMaster;
import com.sarthi.repository.UserMasterRepository;
import com.sarthi.repository.UserRoleMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class mappingImpl implements mappingService {


        private final SleeperPoiIeMappingRepository repository;

        private final UserMasterRepository userMasterRepository;

        private final UserRoleMasterRepository userRoleMasterRepository;

        private final SleeperPincodePoIMappingRepository sleeperPincodePoIMappingRepository;

        private final VendorPlantRepository vendorPlantRepository;

        @Override
        public SleeperPoiIeMappingResDto createMapping(
                SleeperPoiIeMappingReqDto req) {


            UserMaster um = userMasterRepository.findByEmployeeCode(req.getEmployeeCode());


            if(um == null){

                throw new RuntimeException(
                        "User does not exist");
            }


            Integer expectedRoleId = null;

            if(req.getIeType()
                    .equalsIgnoreCase("Main IE")) {

                expectedRoleId = 10;

            } else if(req.getIeType()
                    .equalsIgnoreCase("Process IE")) {

                expectedRoleId = 14;

            } else {

                throw new RuntimeException(
                        "Invalid IE Type");
            }

            boolean roleExists =
                    userRoleMasterRepository
                            .existsByUserIdAndRoleId(
                                    um.getUserId(),
                                    expectedRoleId
                            );

            if(!roleExists){

                throw new RuntimeException(
                        "User role does not match IE Type");
            }


            boolean mappingExists =
                    repository
                            .existsByPoiCodeAndPlantIdAndIeUserIdAndIeType(
                                    req.getPoiCode(),
                                    req.getPlantId(),
                                    um.getUserId(),
                                    req.getIeType()
                            );

            if(mappingExists){

                throw new RuntimeException(
                        "User already mapped to same plant");
            }



            SleeperPoiIeMapping entity =
                    new SleeperPoiIeMapping();

            entity.setPoiCode(req.getPoiCode());

            entity.setPlantId(req.getPlantId());

            entity.setIeUserId(um.getUserId());

            entity.setIeType(req.getIeType());

            entity.setCreatedDate(LocalDateTime.now());

            SleeperPoiIeMapping saved =
                    repository.save(entity);

            return SleeperPoiIeMappingResDto.builder()
                    .id(saved.getId())
                    .poiCode(saved.getPoiCode())
                    .plantId(saved.getPlantId())
                    .ieUserId(saved.getIeUserId())
                    .ieType(saved.getIeType())
                    .createdDate(saved.getCreatedDate())
                    .build();
        }

    @Override
    public List<SleeperCompanyResDto> getAllCompanies() {

        List<SleeperPincodePoIMapping> list =
                sleeperPincodePoIMappingRepository
                        .findAllCompanies();

        return list.stream()
                .map(data -> SleeperCompanyResDto.builder()
                        .companyName(data.getCompanyName())
                        .vendorCode(data.getVendorCode())
                        .poiCode(data.getPoiCode())
                        .build())
                .toList();
    }

    @Override
    public List<String> getPlantIdsByVendorCode(
            String vendorCode) {

        return vendorPlantRepository
                .findPlantIdsByVendorCode(vendorCode);
    }

    @Override
    public List<EmployeeMappingResDto> getMappedEmployees(
            EmployeeMappingFetchReqDto req) {

        List<SleeperPoiIeMapping> mappings =
                repository.findMappedEmployees(
                        req.getCompanyName(),
                        req.getPlantId(),
                        req.getIeType()
                );

        return mappings.stream()
                .map(data -> {

                    String employeeCode =
                            userMasterRepository
                                    .findEmployeeCode(
                                            data.getIeUserId());

                    return EmployeeMappingResDto.builder()
                            .userId(data.getIeUserId())
                            .employeeCode(employeeCode)
                            .ieType(data.getIeType())
                            .plantId(data.getPlantId())
                            .companyName(req.getCompanyName())
                            .build();
                })
                .toList();
    }




}
