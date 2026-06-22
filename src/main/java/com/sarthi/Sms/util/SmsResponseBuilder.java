package com.sarthi.Sms.util;


import com.sarthi.Sms.exception.SmsErrorDetails;
import com.sarthi.constant.AppConstant;

public class SmsResponseBuilder {
    public static SmsApiResponse getErrorResponse(SmsErrorDetails errorDetails) {

    		SmsApiResponseStatus responseStatus =new SmsApiResponseStatus();
    		responseStatus.setMessage(errorDetails.getMessage());
    		responseStatus.setStatusCode(errorDetails.getErrorTypeCode());
    		responseStatus.setErrorCode(errorDetails.getErrorCode());
    		responseStatus.setErrorType(errorDetails.getErrorType());
    		SmsApiResponse response = new SmsApiResponse();
    		response.setResponseStatus(responseStatus);
    		return response;
    }

  
    public static SmsApiResponse getSuccessResponse() {
        return getSuccessResponse(null);
    }

    public static SmsApiResponse getSuccessResponse(Object responseData) {
        SmsApiResponseStatus responseStatus = new SmsApiResponseStatus();
        responseStatus.setStatusCode(AppConstant.API_SUCCESS);
        responseStatus.setMessage("Success");
        SmsApiResponse response = new SmsApiResponse();
        response.setResponseStatus(responseStatus);
        response.setResponseData(responseData);
        return response;
    }
}
