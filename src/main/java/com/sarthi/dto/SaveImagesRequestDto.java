package com.sarthi.dto;

import lombok.Data;
import java.util.List;

@Data
public class SaveImagesRequestDto {
    private String typeOfCall;
    private List<ImageCaptureDto> capturedImages;
    private String shift;
    private String dateOfInspection;
    private String userId;
}
