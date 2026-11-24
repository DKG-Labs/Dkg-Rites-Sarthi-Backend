package com.sarthi.Sms.entity.rolling;

import java.math.BigDecimal;

import com.sarthi.Sms.dto.rolling.BloomVerificationId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="rolling_verification_bloom_yard_detail")
public class VerificationBloomYardEntity {
    // @Id
    // @Column(name = "heat_number", nullable = false, unique = true)
    // private String heatNo;

    // @Column(name = "duty_id", nullable = false)
    // private String dutyId;


    @EmbeddedId
    private BloomVerificationId id;

    @Column(name = "internal_transfer_verification")
    private Boolean internalTransferVerification;

    @Column(name = "heat_register_verification")
    private Boolean heatRegisterVerification;

    @Column(name = "length", precision = 10, scale = 2)
    private BigDecimal length;

    @Column(name="defect")
    private String defect;

    @Column(name="remarks")
    private String remarks;
}
