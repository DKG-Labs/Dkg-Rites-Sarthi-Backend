package com.sarthi.Sms.exception;

public class SmsResourceNotFoundException extends RuntimeException{
    private SmsErrorDetails errorDetails;
    private Throwable throwable;

    public SmsResourceNotFoundException(SmsErrorDetails errorDetails, Throwable throwable) {
        super();
        this.errorDetails = errorDetails;
        this.throwable = throwable;
    }

    public SmsResourceNotFoundException(SmsErrorDetails errorDetails) {
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
