package com.sarthi.service.certificate;

import com.sarthi.dto.certificate.RawMaterialCertificateDto;
import com.sarthi.dto.certificate.ProcessMaterialCertificateDto;
import com.sarthi.dto.certificate.FinalCertificateDto;
import com.sarthi.dto.certificate.IcReportDataResponse;
import java.util.Map;


/**
 * Service interface for generating Inspection Certificates.
 * Handles data aggregation from multiple tables to create certificate data.
 */
public interface CertificateService {

    /**
     * Generate Raw Material Inspection Certificate data by IC Number
     * @param icNumber - Inspection Call Number (e.g., RM-IC-1767772023499)
     * @return RawMaterialCertificateDto with all certificate data
     */
    RawMaterialCertificateDto generateRawMaterialCertificate(String icNumber);

    /**
     * Generate Raw Material Inspection Certificate data by Call ID
     * @param callId - Inspection Call ID
     * @return RawMaterialCertificateDto with all certificate data
     */
    RawMaterialCertificateDto generateRawMaterialCertificateById(Long callId);

    /**
     * Generate Process Material Inspection Certificate data by IC Number
     * @param icNumber - Inspection Call Number (e.g., PM-IC-1767772023499)
     * @return ProcessMaterialCertificateDto with all certificate data
     */
    ProcessMaterialCertificateDto generateProcessMaterialCertificate(String icNumber);

    /**
     * Generate Process Material Inspection Certificate data by Call ID
     * @param callId - Inspection Call ID
     * @return ProcessMaterialCertificateDto with all certificate data
     */
    ProcessMaterialCertificateDto generateProcessMaterialCertificateById(Long callId);

    /**
     * Generate Final Material Inspection Certificate data by IC Number
     * @param icNumber - Inspection Call Number (e.g., FM-IC-1767772023499)
     * @return FinalCertificateDto with all certificate data
     */
    FinalCertificateDto generateFinalCertificate(String icNumber);

    /**
     * Generate Final Material Inspection Certificate data by Call ID
     * @param callId - Inspection Call ID
     * @return FinalCertificateDto with all certificate data
     */
    FinalCertificateDto generateFinalCertificateById(Long callId);

    /**
     * Generate IC Report Data for E-Sign workflow.
     * 
     * @param params - Map of all report parameters (CaseNo, CallSNo, BkNo, SetNo, isDigitallySign, etc.)
     * @return IcReportDataResponse with status and response text (PDF/Data)
     */
    IcReportDataResponse generateReportData(Map<String, String> params);
}


