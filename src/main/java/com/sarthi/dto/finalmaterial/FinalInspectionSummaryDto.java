package com.sarthi.dto.finalmaterial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.sarthi.dto.ImageCaptureDto;

/**
 * DTO for Final Inspection Summary
 * Stores packing verification and overall inspection status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinalInspectionSummaryDto {

    private String inspectionCallNo;

    // ---- PACKING VERIFICATION ----
    private Boolean packedInHdpe;
    private Boolean cleanedWithCoating;

    // ---- STATUS ----
    private String inspectionStatus;

    // Images captured with geofence data
    private List<ImageCaptureDto> capturedImages;

    // ---- AUDIT FIELDS ----
    private String createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime createdAt;
    private String updatedBy;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime updatedAt;
}
