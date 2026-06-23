package com.sarthi.Sms.exception;

public class SmsInvalidArgumentException extends RuntimeException {
    private SmsErrorDetails errorDetails;
    private Throwable throwable;

    public SmsInvalidArgumentException(SmsErrorDetails errorDetails, Throwable throwable) {
        super();
        this.errorDetails = errorDetails;
        this.throwable = throwable;
    }

    public SmsInvalidArgumentException(SmsErrorDetails errorDetails) {
        super();
        this.errorDetails = errorDetails;
        this.throwable = null;
    }

    public SmsErrorDetails getErrorDetails(){
        return errorDetails;
    }

    public Throwable getThrowable(){
        return throwable;
    }
}
