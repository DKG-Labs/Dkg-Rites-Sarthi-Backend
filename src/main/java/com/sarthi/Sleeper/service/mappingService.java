package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.mapping.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface mappingService {

    SleeperPoiIeMappingResDto createMapping(
            SleeperPoiIeMappingReqDto req);

    List<SleeperCompanyResDto> getAllCompanies();

    List<String> getPlantIdsByVendorCode(String vendorCode);

    List<EmployeeMappingResDto> getMappedEmployees(EmployeeMappingFetchReqDto req);

    CompanyEmployeeMappingResDto createBulkMapping(CompanyEmployeeMappingReqDto req);

    List<EmployeeRoleResDto> getEmployeesByRoleId(Integer roleId);
}
