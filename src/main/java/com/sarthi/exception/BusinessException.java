package com.sarthi.exception;

public class BusinessException extends RuntimeException {

    private ErrorDetails errorDetails;
    private Throwable throwable;

    public BusinessException(ErrorDetails errorDetails, Throwable throwable) {
        super(errorDetails != null ? errorDetails.getMessage() : null);
        this.errorDetails = errorDetails;
        this.throwable = throwable;
    }

    public BusinessException(ErrorDetails errorDetails) {
        super(errorDetails != null ? errorDetails.getMessage() : null);
        this.errorDetails = errorDetails;
        this.throwable = null;
    }

    public ErrorDetails getErrorDetails() {
        return errorDetails;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}