package com.sarthi.udmcomplaint.dto;

import lombok.Data;

/** Complete Joint Inspection (JI Report PDF is the multipart "file" part). */
@Data
public class CompleteJiRequest {
    private String actualJiDate;    // yyyy-MM-dd
    private String jiOutcome;
    private String attributableTo;
    private String remarks;
}
