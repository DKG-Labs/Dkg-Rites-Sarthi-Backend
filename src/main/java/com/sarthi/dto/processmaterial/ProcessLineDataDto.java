package com.sarthi.dto.processmaterial;

import lombok.Data;
import java.util.List;

/**
 * DTO for all submodule data for a single production line.
 * Contains data from all process stages (shearing, turning, MPI, etc.)
 */
@Data
public class ProcessLineDataDto {

    private String lineNo;
    private String poNo;
    private String inspectionCallNo;
    private String lotNo;
    private String shift;

    // Calibration Documents
    private List<ProcessCalibrationDocumentsDTO> calibrationDocuments;

    // Static Periodic Checks
    private List<ProcessStaticPeriodicCheckDTO> staticPeriodicChecks;

    // Shearing Data
    private List<ProcessShearingDataDTO> shearingData;

    // Turning Data
    private List<ProcessTurningDataDTO> turningData;

    // MPI Data
    private List<ProcessMpiDataDTO> mpiData;

    // Forging Data
    private List<ProcessForgingDataDTO> forgingData;

    // Quenching Data
    private List<ProcessQuenchingDataDTO> quenchingData;

    // Tempering Data
    private List<ProcessTemperingDataDTO> temperingData;

    // Final Check Data (8-Hour Grid)
    private List<ProcessFinalCheckDataDTO> finalCheckData;

    // Testing & Finishing Data (8-Hour Grid)
    private List<ProcessTestingFinishingDataDTO> testingFinishingData;

    // Oil Tank Counter (if applicable)
    private ProcessOilTankCounterDTO oilTankCounter;

    // Line Final Result (Summary with IE remarks, status, quantities)
    private ProcessLineFinalResultDto lineFinalResult;

    // Line-level remarks
    private String remarks;
}

