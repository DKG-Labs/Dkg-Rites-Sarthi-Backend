package com.sarthi.Sms.service;

import com.sarthi.Sms.dto.sms.common.DutyStatusResDto;
import com.sarthi.Sms.dto.sms.common.EndDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyResDto;
import com.sarthi.Sms.dto.shortrail.IdDto;
import com.sarthi.Sms.dto.shortrail.MasterByIdDto;
import com.sarthi.Sms.dto.shortrail.ShortRailMasterDto;
import com.sarthi.Sms.dto.shortrail.ShortRailMasterResDto;
import com.sarthi.Sms.dto.shortrail.ShortRailSummaryResDto;
import com.sarthi.Sms.dto.shortrail.SriRgRsDeleteDto;
import com.sarthi.Sms.dto.shortrail.WorkstationRemarksDto;
import java.util.List;

public interface ShortRailService {

    public StartDutyResDto startDuty(String authorizationHeader, StartDutyReqDto request);
    public void endDuty(EndDutyReqDto request);
    public DutyStatusResDto checkDutyStatus(String authorizationHeader);
    public StartDutyResDto getOngoingDutyDtls(String ah);
    public void saveShortRailMasterDetails(ShortRailMasterDto req);
    public List<ShortRailMasterResDto> getShortRailMasterByDutyId(String dutyId);
    public ShortRailMasterResDto getShortRailMasterById(MasterByIdDto req);
    public List<ShortRailSummaryResDto> getShortRailSummaries(String dutyId);
    public void deleteRejectionAnalysisById(IdDto req);
    public void deleteInspectedRgRs(SriRgRsDeleteDto req);
    public List<WorkstationRemarksDto> getWorkStationRemarks(String dutyId);
    public void updateShortRailMasterDetails(ShortRailMasterDto req);
}
