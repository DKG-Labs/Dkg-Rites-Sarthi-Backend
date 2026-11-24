package com.sarthi.Sms.dto.iso;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BloomYardDto {
    private String heatNumber;
    private Boolean internalTransferVerification;
    private Boolean heatRegisterVerification;
}
