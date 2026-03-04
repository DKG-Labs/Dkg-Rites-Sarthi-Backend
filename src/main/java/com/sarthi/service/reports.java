package com.sarthi.service;

import com.sarthi.dto.PoInspection2ndLevelSerialStatusDto;
import com.sarthi.dto.reports.DashboardSummaryDto;
import com.sarthi.dto.reports.FourthLevelInspectionDto;
import com.sarthi.dto.reports.PoInspection1stLevelStatusDto;
import com.sarthi.dto.reports.PoInspection3rdLevelCallStatusDto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface reports {

    public List<PoInspection1stLevelStatusDto> getPoInspection1stLevelStatusList();

    public List<PoInspection2ndLevelSerialStatusDto> getSerialStatusByPoNo(String poNo);

    public List<PoInspection3rdLevelCallStatusDto> getCallWiseStatusBy(String poNo, String serialNo);

    public Page<PoInspection3rdLevelCallStatusDto> getCallWiseStatusBySerialNo(
            String poNo,
            String serialNo,
            int page,
            int size);

    public List<FourthLevelInspectionDto> getFourthLevelReport(String callId);

    public DashboardSummaryDto getDashboardSummary();

    public double getAvgProductionPerDay();
}
