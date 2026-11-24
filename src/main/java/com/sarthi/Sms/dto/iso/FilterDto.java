package com.sarthi.Sms.dto.iso;

import lombok.Data;

@Data
public class FilterDto {
    private String date; 
    private String shift;
    private String sms; 
    private String railGrade;
    private String railSection;
    private String mill;
    private String jointNo;
}
