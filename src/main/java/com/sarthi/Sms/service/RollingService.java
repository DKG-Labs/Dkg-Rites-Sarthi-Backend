package com.sarthi.Sms.service;

import java.util.List;

import com.sarthi.Sms.dto.sms.common.DutyStatusResDto;
import com.sarthi.Sms.dto.sms.common.EndDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyResDto;
import com.sarthi.Sms.dto.rolling.RailRollingResDto;
import com.sarthi.Sms.dto.rolling.RollingVerificationDto;
import com.sarthi.Sms.dto.rolling.SaveBatchReqDto;
import com.sarthi.Sms.dto.rolling.SaveRailDtlsReqDto;
import com.sarthi.Sms.dto.rolling.SaveRetestSampleReqDto;
import com.sarthi.Sms.dto.rolling.TempObservationDto;
import com.sarthi.Sms.dto.rolling.TestSampleAcptDto;
import com.sarthi.Sms.dto.rolling.TestSampleDto;
import com.sarthi.Sms.dto.rolling.VerificationBloomYardDto;
import com.sarthi.Sms.dto.rolling.VerificationChargingDto;
import com.sarthi.Sms.dto.rolling.ControlHeatReqDto;
import com.sarthi.Sms.dto.rolling.ControlReqDto;
import com.sarthi.Sms.dto.rolling.ControlSampleDtlReqDto;
import com.sarthi.Sms.dto.rolling.FinishingVerificationReqDto;
import com.sarthi.Sms.dto.rolling.HeatAcptTestDtlReqDto;
import com.sarthi.Sms.dto.rolling.HtRailDeleteDto;
import com.sarthi.Sms.dto.rolling.HtSequenceRailIdDto;
import com.sarthi.Sms.dto.rolling.HtSequenceReqDto;
import com.sarthi.Sms.dto.rolling.HtSequenceResDto;
import com.sarthi.Sms.dto.sms.DeleteHeatReqDto;
public interface RollingService {
    public StartDutyResDto startDuty(String ah, StartDutyReqDto req);

    public void endDuty(EndDutyReqDto req);

    public DutyStatusResDto checkDutyStatus(String ah);

    public StartDutyResDto getOngoingDutyDtls(String ah);

    public RailRollingResDto getRollingControlDtls(String dutyId);
    
    public ControlHeatReqDto getControlSampleDtl(ControlSampleDtlReqDto req);

    public void saveControlHeat(ControlHeatReqDto req);

    // public void updateControlHeat(ControlHeatReqDto req);

    public void saveControlDtls(ControlReqDto req);

    public void saveRollingVerification(RollingVerificationDto req);

    public void saveBloomYardDtls(VerificationBloomYardDto req);

    public void saveChargingDtls(VerificationChargingDto req);

    public void saveTempObservationDtls(TempObservationDto req);

    // List<TempObservationReqResDto> getTempObservationDtls(String dutyId);

    public void saveFinishingVerification(FinishingVerificationReqDto req);

    public TestSampleAcptDto getHeatAcptTestDtls(HeatAcptTestDtlReqDto req);

    public void saveAcceptanceTestSample(TestSampleAcptDto req);

    public void saveTestingRailDtls(SaveRailDtlsReqDto req);

    public void saveBatch(SaveBatchReqDto req);

    public HtSequenceResDto getHtSeqOpenBatchDtls();

    public List<String> getMarkedTestSamples(String railId);

    public void saveHtSequenceRail(HtSequenceReqDto req);

    public List<HtSequenceRailIdDto> getHtSeqOpenBatchDtlsV2();

    public void updateHtSequenceRail(HtSequenceReqDto req);

    public void deleteHtRail(HtRailDeleteDto req);

    public void saveRetestSample(SaveRetestSampleReqDto req);

    public List<Integer> getStrandNoList(String heatNumber, String sampleLot);

    public List<TestSampleDto> getAllTestSamples();

    public List<TestSampleDto> getTestSamplesByCurrentShift(String currentDutyId);

    void deleteTestSample(String heatNumber, String type);



    TestSampleAcptDto getTestSampleById(Integer rollingTestSampleId, String type);

    void updateTestSample(TestSampleAcptDto req);

    void updateRetestSample(SaveRetestSampleReqDto req);

    void deleteHeatDtl(DeleteHeatReqDto req);
    public void deleteControlHeat(ControlHeatReqDto req);

    void validateHeatForTesting(String heatNumber);

    // Debug methods for retest sample cleanup
    List<Object> checkExistingRetestSamples(String heatNumber);
    void forceDeleteRetestSamples(String heatNumber);
}
