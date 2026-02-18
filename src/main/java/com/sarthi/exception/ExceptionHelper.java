package com.sarthi.exception;

import com.sarthi.constant.AppConstant;
import com.sarthi.util.ResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class ExceptionHelper {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHelper.class);

    @ExceptionHandler(value = { InvalidInputException.class })
    public ResponseEntity<Object> handleInvalidInputException(InvalidInputException ex,  WebRequest request) {
        ex.printStackTrace();
        return new ResponseEntity<Object>(ResponseBuilder.getErrorResponse(ex.getErrorDetails()), HttpStatus.BAD_REQUEST);
    }

    

    @ExceptionHandler(value = { UnauthorizedException.class })
    public ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException ex,  WebRequest request) {
        ex.printStackTrace();
        return new ResponseEntity<Object>(ResponseBuilder.getErrorResponse(ex.getErrorDetails()), HttpStatus.BAD_REQUEST);
    }

    

    @ExceptionHandler(value = { BusinessException.class })
    public ResponseEntity<Object> handleBusinessException(BusinessException ex,  WebRequest request) {
        ex.printStackTrace();
        return new ResponseEntity<Object>(ResponseBuilder.getErrorResponse(ex.getErrorDetails()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = { FilesNotFoundException.class })
    public ResponseEntity<Object> handleFilesNotFoundException(BusinessException ex,  WebRequest request) {
        ex.printStackTrace();
        return new ResponseEntity<Object>(ResponseBuilder.getErrorResponse(ex.getErrorDetails()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = { Exception.class })
    public ResponseEntity<Object> handleException(Exception ex,  WebRequest request) {
        ex.printStackTrace();
        System.out.println("EXCEPTION EX: " + ex);
        ErrorDetails errorDetails = new ErrorDetails(AppConstant.INTER_SERVER_ERROR, AppConstant.ERROR_TYPE_CODE_INTERNAL,
                AppConstant.ERROR_TYPE_ERROR, ex.getMessage());
        return new ResponseEntity<Object>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = { EmailNotSentException.class })
    public ResponseEntity<Object> handleEmailNotSentException(EmailNotSentException ex, WebRequest request) {
        ex.printStackTrace();
        return new ResponseEntity<Object>(ResponseBuilder.getErrorResponse(ex.getErrorDetails()), HttpStatus.BAD_REQUEST);
    }


}