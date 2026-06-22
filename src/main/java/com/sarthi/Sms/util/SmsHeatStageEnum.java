package com.sarthi.Sms.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SmsHeatStageEnum {

    NEW(0, "New Heat"),
    CONVERTER(1, "Converter"),
    DEGASSING(2, "Degassing"),
    CASTING(3, "Casting"),
    CHEMICAL(4, "Chemical Analysis"),
    BLOOM(5, "Bloom Cutting"),
    COMPLETE(6, "Complete");

    private final int code;
    private final String description;

    public static int getCodeFromDesc(String desc){
        for(SmsHeatStageEnum stage : SmsHeatStageEnum.values()){
            if(stage.getDescription().equalsIgnoreCase(desc)){
                return stage.getCode();
            }
        }

        return -1;
    }
}
