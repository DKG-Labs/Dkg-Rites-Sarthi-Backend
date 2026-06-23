package com.sarthi.Sms.entity.sms;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name="bloom_detail_sms2")
public class BloomDtlSms2Entity {
    @Id
    @Column(name = "cast_number")
    private String castNo;

    @Column(name = "duty_id", nullable = false)
    private String dutyId;

    @Column(name = "bloom_identification", nullable = false)
    private String bloomIdentification;

    @Column(name = "length_of_blooms", nullable = false)
    private BigDecimal lengthOfBlooms;

    @Column(name = "surface_condition_of_blooms", nullable = false)
    private String surfaceConditionOfBlooms;

    @Column(name = "number_of_prime_blooms_rejected", nullable = false)
    private Integer noOfPrimeBloomsRejected;

    @Column(name = "number_of_co_blooms_rejected", nullable = false)
    private Integer noOfCoBloomsRejected;

    @Column(name = "remark", nullable = false)
    private String remark;
}