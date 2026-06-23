package com.sarthi.Sms.util;

import lombok.Data;

@Data
public class SmsApiResponseStatus {
    private int statusCode;
	private String message;
	private Integer errorCode;
	private String errorType = null;
}
