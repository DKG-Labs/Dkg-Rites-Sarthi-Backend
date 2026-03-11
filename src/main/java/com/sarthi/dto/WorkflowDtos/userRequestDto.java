package com.sarthi.dto.WorkflowDtos;

import com.sarthi.dto.IePinPoiDto;
import com.sarthi.dto.IePoiMappingDto;
import lombok.Data;

import java.util.List;

@Data
public class userRequestDto {

    private Integer userId;
    private String userName;
    private String password;
    private List<String> roleNames;
    // private String roleName;
    private String email;
    private String mobileNumber;
    private String employeeId;
    private String createdBy;

    private String clusterName;
    private String regionName;
    private Integer priority; // if role secondary ie this mandetory

    private List<Integer> ieUserIds; // IE users mapped under Process IE

    private String employeeCode;
    private Integer ritesEmployeeCode; // only REGULAR / CONTRACTUAL
    private String employmentType; // REGULAR / CONTRACTUAL / MPA
    private String fullName;
    private String shortName;
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
    private String dateOfBirth;
    private String designation;
    private String discipline;

    // IE
    private String rio; // NRIO / ERIO
    private String currentCityOfPosting;
    private Integer metalStampNo;

    // IE product + pin + poi
    private List<IePinPoiDto> iePinPoiList;

    // IE → Controlling Manager
    private Integer controllingManagerUserId;

    private String productType;

    private List<IePoiMappingDto> iePoiMappings;

}
