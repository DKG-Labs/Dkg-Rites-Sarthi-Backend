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
import java.util.ArrayList;
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

            if (req == null || req.getEmployeeCode() == null || req.getEmployeeCode().trim().isEmpty()) {
                throw new RuntimeException("Employee Code is required");
            }

            List<UserMaster> users = userMasterRepository.findAllByEmployeeCode(req.getEmployeeCode().trim());

            if (users == null || users.isEmpty()) {
                throw new RuntimeException("User does not exist with Employee Code: " + req.getEmployeeCode());
            }

            if (users.size() > 1) {
                throw new RuntimeException("Multiple duplicate records (" + users.size() + ") found for Employee Code '" 
                        + req.getEmployeeCode() + "' in User Master. Please resolve duplicate records in User Master.");
            }

            UserMaster um = users.get(0);

            Integer expectedRoleId = null;

            if (req.getIeType() != null && req.getIeType().equalsIgnoreCase("Main IE")) {
                expectedRoleId = 10;
            } else if (req.getIeType() != null && req.getIeType().equalsIgnoreCase("Process IE")) {
                expectedRoleId = 14;
            } else {
                throw new RuntimeException("Invalid IE Type: '" + req.getIeType() + "'. Expected 'Main IE' or 'Process IE'");
            }

            boolean roleExists =
                    userRoleMasterRepository
                            .existsByUserIdAndRoleId(
                                    um.getUserId(),
                                    expectedRoleId
                            );

            if(!roleExists){
                throw new RuntimeException(
                        "User role does not match IE Type (Expected role: " + req.getIeType() + ")");
            }

            if (req.getId() != null) {
                java.util.Optional<SleeperPoiIeMapping> existingOpt = repository.findById(req.getId());
                if (existingOpt.isPresent()) {
                    SleeperPoiIeMapping existing = existingOpt.get();
                    existing.setPoiCode(req.getPoiCode());
                    existing.setPlantId(req.getPlantId());
                    existing.setIeUserId(um.getUserId());
                    existing.setIeType(req.getIeType());
                    SleeperPoiIeMapping saved = repository.save(existing);
                    return SleeperPoiIeMappingResDto.builder()
                            .id(saved.getId())
                            .poiCode(saved.getPoiCode())
                            .plantId(saved.getPlantId())
                            .ieUserId(saved.getIeUserId())
                            .ieType(saved.getIeType())
                            .createdDate(saved.getCreatedDate())
                            .build();
                }
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
                        "User is already mapped to plant " + req.getPlantId() + " as " + req.getIeType());
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


    @Override
    public CompanyEmployeeMappingResDto createBulkMapping(
            CompanyEmployeeMappingReqDto req) {

        List<String> successEmployees = new ArrayList<>();

        List<String> failedEmployees = new ArrayList<>();

        Integer expectedRoleId = null;


        if(req.getIeType().equalsIgnoreCase("Main IE")) {

            expectedRoleId = 10;

        }
        else if(req.getIeType().equalsIgnoreCase("Process IE")) {

            expectedRoleId = 14;

        }
        else {

            throw new RuntimeException("Invalid IE Type");
        }


        for(String employeeCode : req.getEmployeeCodes()) {

            try {
                if (employeeCode == null || employeeCode.trim().isEmpty()) {
                    continue;
                }

                List<UserMaster> users = userMasterRepository.findAllByEmployeeCode(employeeCode.trim());

                // USER VALIDATION
                if(users == null || users.isEmpty()) {
                    failedEmployees.add(
                            employeeCode + " -> User not found");
                    continue;
                }

                if(users.size() > 1) {
                    failedEmployees.add(
                            employeeCode + " -> Multiple duplicate records (" + users.size() + ") found in User Master");
                    continue;
                }

                UserMaster user = users.get(0);

                // ROLE VALIDATION
                boolean roleExists =
                        userRoleMasterRepository
                                .existsByUserIdAndRoleId(
                                        user.getUserId(),
                                        expectedRoleId
                                );

                if(!roleExists) {

                    failedEmployees.add(
                            employeeCode + " -> Invalid role");

                    continue;
                }

                // DUPLICATE VALIDATION
                boolean mappingExists =
                        repository
                                .existsByPoiCodeAndPlantIdAndIeUserIdAndIeType(
                                        req.getPoiCode(),
                                        req.getPlantId(),
                                        user.getUserId(),
                                        req.getIeType()
                                );

                if(mappingExists) {

                    failedEmployees.add(
                            employeeCode + " -> Already mapped");

                    continue;
                }


                SleeperPoiIeMapping entity =
                        new SleeperPoiIeMapping();

                entity.setPoiCode(req.getPoiCode());

                entity.setPlantId(req.getPlantId());

                entity.setIeUserId(user.getUserId());

                entity.setIeType(req.getIeType());

                entity.setCreatedDate(LocalDateTime.now());

                repository.save(entity);

                successEmployees.add(employeeCode);

            } catch (Exception ex) {

                failedEmployees.add(
                        employeeCode + " -> " + ex.getMessage());
            }
        }

        return CompanyEmployeeMappingResDto.builder()
                .successEmployees(successEmployees)
                .failedEmployees(failedEmployees)
                .build();
    }


    @Override
    public List<EmployeeRoleResDto> getEmployeesByRoleId(
            Integer roleId) {

        List<UserMaster> users =
                userMasterRepository
                        .findUsersByRoleId(roleId);

        return users.stream()
                .map(user -> EmployeeRoleResDto.builder()
                        .userId(user.getUserId())
                        .employeeCode(user.getEmployeeCode())
                        .fullName(user.getFullName())
                        .userName(user.getUsername())
                        .build())
                .toList();
    }
}
