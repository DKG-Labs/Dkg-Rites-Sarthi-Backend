package com.sarthi.service;

import com.sarthi.dto.IcDtos.InspectionCallRequestDto;
import com.sarthi.dto.IcDtos.RmInspectionDetailsRequestDto;
import com.sarthi.entity.rawmaterial.InspectionCall;
import org.springframework.stereotype.Service;

@Service
public interface InspectionCallService {

    public InspectionCall createInspectionCall(
            InspectionCallRequestDto icRequest,
            RmInspectionDetailsRequestDto rmRequest);

    /**
     * Check if an inspection call already exists for a given PO Serial No
     * 
     * @param poSerialNo - PO Serial Number to check
     * @return true if at least one inspection call exists, false otherwise
     */
    boolean checkIfCallExistsForPoSerial(String poSerialNo);
}
