package com.sarthi.service;

import com.sarthi.dto.IcDtos.InspectionCallRequestDto;
import com.sarthi.dto.IcDtos.ProcessInspectionDetailsRequestDto;
import com.sarthi.entity.rawmaterial.InspectionCall;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProcessInspectionCallService {

    /**
     * Create a new Process Inspection Call
     * @param icRequest - Inspection Call basic details
     * @param processDetails - List of Process Inspection Details (lot-heat combinations)
     * @return Created InspectionCall entity
     */
    InspectionCall createProcessInspectionCall(
            InspectionCallRequestDto icRequest,
            List<ProcessInspectionDetailsRequestDto> processDetails
    );

    InspectionCall modifyProcessInspectionCall(
            String icNumber,
            InspectionCallRequestDto icDto,
            List<ProcessInspectionDetailsRequestDto> processDetailsList);
}

