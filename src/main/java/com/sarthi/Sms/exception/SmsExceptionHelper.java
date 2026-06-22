package com.sarthi.Sms.exception;


import com.sarthi.constant.AppConstant;
import com.sarthi.Sms.util.SmsResponseBuilder;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class SmsExceptionHelper {

     // Generic Exception Handler
     @ExceptionHandler(value = { Exception.class })
     public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        System.out.println(ex);
        System.out.println(request);
        return new ResponseEntity<>(SmsResponseBuilder.getErrorResponse(new SmsErrorDetails(AppConstant.INTERNAL_SERVER_ERROR, AppConstant.ERROR_TYPE_CODE_INTERNAL, AppConstant.ERROR_TYPE_INTERNAL, "Internal Server Error. Please contact support.")), HttpStatus.INTERNAL_SERVER_ERROR);
     }
    
    @ExceptionHandler(value = { SmsResourceNotFoundException.class })
    public ResponseEntity<Object> handleResourceNotFoundException(SmsResourceNotFoundException ex, WebRequest request){
        return new ResponseEntity<Object>(SmsResponseBuilder.getErrorResponse(ex.getErrorDetails()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = { SmsInvalidArgumentException.class })
    public ResponseEntity<Object> handleInvalidArgumentException(SmsInvalidArgumentException ex, WebRequest request){
        return new ResponseEntity<Object>(SmsResponseBuilder.getErrorResponse(ex.getErrorDetails()), HttpStatus.BAD_REQUEST);
    }
}
