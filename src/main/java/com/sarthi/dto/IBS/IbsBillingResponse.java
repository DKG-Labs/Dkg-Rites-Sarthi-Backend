package com.sarthi.dto.IBS;

import lombok.Data;

import java.util.List;

@Data
public class IbsBillingResponse {

        private Integer resultFlag;

        private String message;

        private List<BillDetailDto> billDetails;

        private String billDetailsError;

        private List<PaymentDetailDto> paymentDetails;

        private String paymentDetailsError;

}
