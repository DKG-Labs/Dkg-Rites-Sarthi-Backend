package com.sarthi.Sms.service;

import com.sarthi.Sms.dto.calibration.BulkCalibrationReqDto;
import com.sarthi.Sms.dto.calibration.CalibrationDetailDto;
import com.sarthi.Sms.dto.calibration.CalibrationReqDto;
import com.sarthi.Sms.dto.calibration.CalibrationCsvImportDto;
import com.sarthi.Sms.dto.calibration.CalibrationCsvImportResponseDto;
import com.sarthi.Sms.dto.calibration.BulkCalibrationCsvImportDto;
import com.sarthi.Sms.dto.sms.common.DutyStatusResDto;
import com.sarthi.Sms.dto.sms.common.EndDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyResDto;
import java.util.List;
import java.util.Map;

public interface CalibrationService {

    /**
     * Starts a calibration duty.
     *
     * @param ah           Additional header or identifier.
     * @param startDutyReq Request data for starting the duty.
     * @return Response with duty start details.
     */
    public StartDutyResDto startDuty(String ah, StartDutyReqDto startDutyReq);

    /**
     * Ends a calibration duty.
     *
     * @param req Request data for ending the duty.
     */
    public void endDuty(EndDutyReqDto req);

    /**
     * Checks the status of a calibration duty.
     *
     * @param ah Identifier or header for the request.
     * @return Response with the duty status.
     */
    public DutyStatusResDto checkDutyStatus(String ah);

    void saveCalibration(String authHeader, CalibrationReqDto request);

    CalibrationDetailDto getLatestCalibrationDetail(String serialNumber);

    List<CalibrationDetailDto> getAllLatestCalibrations();

    void saveBulkCalibration(String authorizationHeader,BulkCalibrationReqDto bulkCalibrationRequestDto );
    
    public StartDutyResDto getOngoingDutyDtls(String ah);

    public Map<String, List<String> > getMmVrnrWmcInstList();

    void deleteCalibration(String serialNumber);

    boolean isExistingCalibration(String serialNumber);

    CalibrationCsvImportResponseDto importCalibrationFromCsv(String authHeader, CalibrationCsvImportDto csvData);

    CalibrationCsvImportResponseDto importBulkCalibrationFromCsv(String authHeader, BulkCalibrationCsvImportDto bulkCsvData);

}
