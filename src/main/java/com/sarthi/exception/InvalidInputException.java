package com.sarthi.exception;

public class InvalidInputException extends RuntimeException {

    private ErrorDetails errorDetails;
    private Throwable throwable;

    public InvalidInputException(ErrorDetails errorDetails, Throwable throwable) {
        super(errorDetails != null ? errorDetails.getMessage() : null);
        this.errorDetails = errorDetails;
        this.throwable = throwable;
    }

    public InvalidInputException(ErrorDetails errorDetails) {
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
