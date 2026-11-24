package com.sarthi.Sms.service;

import java.util.List;

import com.sarthi.Sms.dto.sms.common.DutyStatusResDto;
import com.sarthi.Sms.dto.sms.common.EndDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyResDto;
import com.sarthi.Sms.dto.testing.CompletedTestDto;
import com.sarthi.Sms.dto.testing.PendingTestSampleDtlResDto;
import com.sarthi.Sms.entity.testing.CompletedTest;
public interface TestingService {
    public StartDutyResDto startDuty(String ah, StartDutyReqDto req);

    public void endDuty(EndDutyReqDto req);

    public DutyStatusResDto checkDutyStatus(String ah);

    public StartDutyResDto getOngoingDutyDtls(String ah);

    public List<PendingTestSampleDtlResDto> getPendingTestSampleDtls();

    public List<PendingTestSampleDtlResDto> getPendingTestSampleDtlsByUser(String authHeader);

    public void saveCompletedTest(CompletedTestDto testDto);

    public boolean isTestCompleted(String heatNumber, Integer strandNumber, String sampleId, String testType);

    public CompletedTestDto getCompletedTest(String heatNumber, Integer strandNumber, String sampleId, String testType);

    public List<CompletedTest> getAllCompletedTests();

    public void saveTestRecord();
}
