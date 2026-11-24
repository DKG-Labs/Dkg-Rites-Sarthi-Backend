package com.sarthi.Sms.service;

import java.util.List;

import com.sarthi.Sms.dto.dashboard.AiDashboardSummaryDto;
import com.sarthi.Sms.dto.dashboard.AiSystemRailDto;
import com.sarthi.Sms.dto.dashboard.DimensionalInspectionReqDto;
import com.sarthi.Sms.dto.dashboard.DimensionalInspectionResDto;
import com.sarthi.Sms.dto.dashboard.SurfaceInspectionReqDto;
import com.sarthi.Sms.dto.dashboard.SurfaceInspectionResDto;
import com.sarthi.Sms.entity.dashboard.DimensionalInspectionEntity;
import com.sarthi.Sms.service.Impl.RailMillDashboardServiceImpl;
public interface RailMillDashboardService {

    public List<String> getDimensionalInspectionDtls(DimensionalInspectionReqDto req);

    public List<DimensionalInspectionResDto> getDimensionalInspectionDtlByRailId(String railId);

    public List<String> getSurfaceInspectionDtls(SurfaceInspectionReqDto req);

    public List<SurfaceInspectionResDto> getSurfaceInspectionDtlByRailId(String railId);

    public List<AiSystemRailDto> getAiSystemRailsWithHeatNumbers(DimensionalInspectionReqDto req);

    public AiDashboardSummaryDto getAiAccuracyMetrics(DimensionalInspectionReqDto req);

    public RailMillDashboardServiceImpl.ConfusionMatrix calculateDimensionalConfusionMatrix(List<DimensionalInspectionEntity> dimensionalInspections, String railId);
}
