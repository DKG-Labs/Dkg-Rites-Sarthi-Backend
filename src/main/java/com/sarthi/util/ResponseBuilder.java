package com.sarthi.util;

import com.sarthi.constant.AppConstant;
import com.sarthi.dto.summaryDtos.PageResponseDTO;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.exception.ErrorDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseBuilder.class);
    public static APIResponse getErrorResponse(ErrorDetails errorDetails) {

    		APIResponseStatus responseStatus =new APIResponseStatus();
    		responseStatus.setMessage(errorDetails.getMessage());
    		responseStatus.setStatusCode(errorDetails.getErrorTypeCode());
    		responseStatus.setErrorCode(errorDetails.getErrorCode());
    		responseStatus.setErrorType(errorDetails.getErrorType());
    		APIResponse response = new APIResponse();
    		response.setResponseStatus(responseStatus);
    		return response;
    }

  
    public static APIResponse getSuccessResponse() {
        return getSuccessResponse(null);
    }

    public static APIResponse getSuccessResponse(Object responseData) {
        APIResponseStatus responseStatus = new APIResponseStatus();
        responseStatus.setStatusCode(AppConstant.API_SUCCESS);
        APIResponse response = new APIResponse();
        response.setResponseStatus(responseStatus);
        response.setResponseData(responseData);
        return response;
    }

    public static <T> APIResponse getPageSuccessResponse(PageResponseDTO<T> pageData) {

        APIResponseStatus responseStatus = new APIResponseStatus();
        responseStatus.setStatusCode(AppConstant.API_SUCCESS);

        APIResponse response = new APIResponse();
        response.setResponseStatus(responseStatus);
        response.setResponseData(pageData);

        return response;
    }



}
