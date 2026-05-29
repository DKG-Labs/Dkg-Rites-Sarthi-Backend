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

    InspectionCall modifyInspectionCall(
            String icNumber,
            InspectionCallRequestDto icDto,
            RmInspectionDetailsRequestDto rmDto);

    void processDtoFields(
            Object dto,
            Object entity,
            InspectionCall inspection,
            String tableName,
            Integer modificationVersion,
            String modifiedBy);

    void saveModificationHistory(
            InspectionCall inspection,
            Integer modificationVersion,
            String tableName,
            String fieldName,
            Object oldValue,
            Object newValue,
            String modifiedBy);
}
