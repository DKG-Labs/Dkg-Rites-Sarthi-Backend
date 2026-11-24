package com.sarthi.Sms.service;

import com.sarthi.Sms.dto.sms.common.DutyStatusResDto;
import com.sarthi.Sms.dto.sms.common.EndDutyReqDto;
import com.sarthi.Sms.dto.sms.common.ReportReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyResDto;
import com.sarthi.Sms.dto.qct.GetQctInspectionResDto;
import com.sarthi.Sms.dto.qct.PendingTestSummaryDto;
import com.sarthi.Sms.dto.qct.QctCenterLineDto;
import com.sarthi.Sms.dto.qct.QctDtlResDto;
import com.sarthi.Sms.dto.qct.QctFatigueDto;
import com.sarthi.Sms.dto.qct.QctFcgrDto;
import com.sarthi.Sms.dto.qct.QctFractureToughnessDto;
import com.sarthi.Sms.dto.qct.QctNewTestSampleDto;
import com.sarthi.Sms.dto.qct.QctRecordDto;
import com.sarthi.Sms.dto.qct.QctResidualDto;
//import com.sarthi.Sms.dto.qct.QctTestingDetailDto;
import com.sarthi.Sms.dto.qct.QctTestingDetailDto;
import com.sarthi.Sms.dto.qct.QctTestingMasterDto;
import com.sarthi.Sms.dto.qct.SaveQctInspectionReqDto;
import java.util.List;

public interface QctService {
    public StartDutyResDto startDuty(String authorizationHeader, StartDutyReqDto req);

    public DutyStatusResDto checkDutyStatus(String authorizationHeader);

    public StartDutyResDto getOngoingDutyDtls(String ah);

    public void endDuty(EndDutyReqDto req);

    // public void saveQctInspection(String authorizationHeader, SaveQctInspectionReqDto requestDto) ;

    // public GetQctInspectionResDto getQctInspection(String dutyId);

    // public List<GetQctInspectionResDto> getAllQctInspections();

    // public void saveQctTestingMaster(String authorizationHeader, QctTestingMasterDto requestDto) ;

    // public void saveQctTestingDetail(String authorizationHeader, QctTestingDetailDto requestDto);

    public void saveNewTestSample(QctNewTestSampleDto req);

    public void saveFatigue(QctFatigueDto req);

    public void saveResidual(QctResidualDto req);

    public void saveFcgr(QctFcgrDto req);

    public List<PendingTestSummaryDto> getPendingTestSummary();

    public List<QctDtlResDto> getQctDtlByDutyId(String dutyId);

    public QctDtlResDto getQctDtlByQctId(Integer qctId);

    public void saveFractureToughness(QctFractureToughnessDto req);

    public void saveCenterLine(QctCenterLineDto req);

    public List<QctRecordDto> getQctRecord(ReportReqDto req);

    public void updateTestSample(QctNewTestSampleDto req);

    public void deleteSample(Integer qctId);
}
