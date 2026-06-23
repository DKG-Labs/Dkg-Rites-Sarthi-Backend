package com.sarthi.Sms.exception;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class SmsErrorDetails {
    private int errorCode;
    private int errorTypeCode;
    private String errorType;
    private String message;

    public SmsErrorDetails(int errorCode, int errorTypeCode, String errorType, String message) {
        this.errorCode = errorCode;
        this.errorTypeCode = errorTypeCode;
        this.errorType = errorType;
        this.message = message;
    }

    @Override
    public String toString() {
        return "ErrorDetails [errorCode=" + errorCode + ", errorTypeCode="
                + errorTypeCode + ", errorType=" + errorType + ", message="
                + message + "]";
    }

}
