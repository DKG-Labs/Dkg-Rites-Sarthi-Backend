package com.sarthi.Sms.exception;


import com.sarthi.constant.AppConstant;
import com.sarthi.Sms.util.SmsResponseBuilder;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SmsExceptionHelper {
    @ExceptionHandler(value = { SmsResourceNotFoundException.class })
    public ResponseEntity<Object> handleResourceNotFoundException(SmsResourceNotFoundException ex, WebRequest request){
        return new ResponseEntity<Object>(SmsResponseBuilder.getErrorResponse(ex.getErrorDetails()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = { SmsInvalidArgumentException.class })
    public ResponseEntity<Object> handleInvalidArgumentException(SmsInvalidArgumentException ex, WebRequest request){
        return new ResponseEntity<Object>(SmsResponseBuilder.getErrorResponse(ex.getErrorDetails()), HttpStatus.BAD_REQUEST);
    }
}
