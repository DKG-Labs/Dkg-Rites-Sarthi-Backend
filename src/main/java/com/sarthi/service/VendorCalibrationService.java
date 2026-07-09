package com.sarthi.service;

import com.sarthi.dto.Calibration.CreateIeVendorCalibrationInspectionRequestDto;
import com.sarthi.dto.Calibration.IeVendorCalibrationInspectionResponseDto;
import com.sarthi.dto.VendorCalibrationHeaderRequestDto;
import com.sarthi.dto.VendorCalibrationHeaderResponseDto;

import java.util.List;
import java.util.Map;

public interface VendorCalibrationService {

    void submitBulkRegistration(Map<String, Object> payload, String userId);

    VendorCalibrationHeaderResponseDto createOrUpdateCalibrationGroup(VendorCalibrationHeaderRequestDto requestDto, String userId);

    List<VendorCalibrationHeaderResponseDto> getCalibrationsByVendor(String vendorCode);

    VendorCalibrationHeaderResponseDto getCalibrationGroupById(Long id);
    
    List<VendorCalibrationHeaderResponseDto> getCalibrationsByCreatedBy(String createdBy);
    
    List<VendorCalibrationHeaderResponseDto> getCalibrationsByCallNo(String callNo);

    void deleteCalibrationGroup(Long id);

    void deleteCalibrationDetail(Long detailId);

    VendorCalibrationHeaderResponseDto updateCalibrationDetail(Long detailId, com.sarthi.dto.VendorCalibrationDetailDto detailDto, String userId);

    public IeVendorCalibrationInspectionResponseDto createInspection(
            CreateIeVendorCalibrationInspectionRequestDto requestDto);

    public IeVendorCalibrationInspectionResponseDto getInspectionByCallNo(String callNo);

    public List<VendorCalibrationHeaderResponseDto> getByVendorCode(String vendorCode);
}
