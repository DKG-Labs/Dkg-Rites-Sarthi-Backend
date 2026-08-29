package com.sarthi.service;

import com.sarthi.dto.*;
import com.sarthi.dto.ForgotPasswordRequestDto;
import com.sarthi.dto.MFA.VerifyOtpRequestDto;
import com.sarthi.dto.WorkflowDtos.userRequestDto;
import com.sarthi.entity.IePincodePoiMapping;
import com.sarthi.entity.PincodePoIMapping;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public interface UserService {

    public UserDto createUser(userRequestDto userDto);

    public LoginResponseDto login(LoginRequestDto loginRequestDto);

   // public LoginResponseDto loginBasedOnType(LoginRequestBasedTypeDto loginDto);
    public Object loginBasedOnType(LoginRequestBasedTypeDto loginDto);
    public void forgotPassword(ForgotPasswordRequestDto requestDto);

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

    public List<String> getEmployeeCodesByCallNo(String callNo);

    public String updateCompanyIeMapping(String poiCode, List<IePinPoiDto> newList);

    public List<IePincodePoiMapping> getEmployeesByPoi(String poiCode);

    public Map<String, Object> getProcessAndIeUsers(String poiCode);

    public String updatePoiIeUsers(String poiCode,
                                   List<Long> newIeUserIds,
                                   String createdBy);

    public String getPlaceOfInspection(String icNumber);


    public String mapProcessIe(PoiProcessIeRequestDto dto);
    
    // User Management
    public List<UserDto> getAllUsers();
    public UserDto updateUser(userRequestDto userDto);
    public void deleteUser(Integer userId);
    public String updateUserRegion(Integer userId, String newRegion);
    public String updateUserRole(Integer userId, List<String> newRoles);

    public String updateUnitContact(String poiCode, String contactPerson, String contactPersonNumber);

    public Object createOrUpdateErcVendor(com.sarthi.dto.ErcVendorCreationDto dto);

    public Object getErcVendorDetails(Integer userId);

    LoginResponseDto verifyOtp(VerifyOtpRequestDto request);
}
