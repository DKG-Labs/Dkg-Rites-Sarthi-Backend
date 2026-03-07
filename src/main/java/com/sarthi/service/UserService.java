package com.sarthi.service;

import com.sarthi.dto.*;
import com.sarthi.dto.WorkflowDtos.userRequestDto;
import com.sarthi.entity.PincodePoIMapping;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    public UserDto createUser(userRequestDto userDto);

    public LoginResponseDto login(LoginRequestDto loginRequestDto);

    public LoginResponseDto loginBasedOnType(LoginRequestBasedTypeDto loginDto);

    public Object mapProcessIe(Long userId,
            ProcessIeMappingRequestDto dto,
            String createdBy);

    public Object setupIe(Long userId, IeSetupRequestDto dto);

    public UserDto createUserAndRole(userRequestDto userDto);

    public java.util.List<String> getAllRoleNames();

    public java.util.List<UserDto> getUsersByRole(String roleName);

    public java.util.List<String> getAllCompanies();

    public java.util.List<String> getUnitsByCompany(String companyName);

    public PincodePoIMapping getMappingByCompanyAndUnit(String companyName, String unitName);



    public List<CompanyUnitIeResponseDto> getAllCompanyMappedIe();

    List<CompanyUnitProcessIeDto> getCompanyUnitProcessIe();
}
