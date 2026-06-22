package com.sarthi.Sms.util;

import lombok.Data;

@Data
public class SmsApiResponse {
    private SmsApiResponseStatus responseStatus;
	private Object responseData;
}
