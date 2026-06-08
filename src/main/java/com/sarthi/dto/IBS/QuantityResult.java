package com.sarthi.dto.IBS;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuantityResult {

    private Integer quantityOffered;

    private Integer quantityPassed;

    private Integer quantityRejected;
}