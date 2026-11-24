package com.sarthi.Sms.entity.visualInspection;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class ViAcptId implements Serializable {

    @Column(name = "rail_id", nullable = false)
    private String railId;

    // @Column(name = "heat_number", nullable = false)
    // private String heatNo;

    @Column(name = "accepted_length", nullable = false)
    private BigDecimal acceptedLength;
}
