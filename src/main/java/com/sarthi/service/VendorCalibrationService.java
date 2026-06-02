package com.sarthi.service;

import com.sarthi.dto.Calibration.CreateIeVendorCalibrationInspectionRequestDto;
import com.sarthi.dto.Calibration.IeVendorCalibrationInspectionResponseDto;
import com.sarthi.dto.VendorCalibrationHeaderRequestDto;
import com.sarthi.dto.VendorCalibrationHeaderResponseDto;

import java.util.List;

public interface VendorCalibrationService {

    VendorCalibrationHeaderResponseDto createOrUpdateCalibrationGroup(VendorCalibrationHeaderRequestDto requestDto, String userId);

    List<VendorCalibrationHeaderResponseDto> getCalibrationsByVendor(String vendorCode);

    VendorCalibrationHeaderResponseDto getCalibrationGroupById(Long id);

    void deleteCalibrationGroup(Long id);

    void deleteCalibrationDetail(Long detailId);

    public IeVendorCalibrationInspectionResponseDto createInspection(
            CreateIeVendorCalibrationInspectionRequestDto requestDto);

    public IeVendorCalibrationInspectionResponseDto getInspectionByCallNo(String callNo);

    public List<VendorCalibrationHeaderResponseDto> getByVendorCode(String vendorCode);
}
