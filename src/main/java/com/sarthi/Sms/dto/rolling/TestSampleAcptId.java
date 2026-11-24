package com.sarthi.Sms.dto.rolling;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestSampleAcptId implements Serializable {
    @Column(name = "sample_id", length = 50)
    private String sampleId;
    
    @Column(name = "heat_number", length = 50)
    private String heatNo;

    @Column(name = "sample_type", length = 50)
    private String sampleType;

    @Column(name = "strand")
    private Integer strand;

    @Column(name = "sample_lot", length = 50)
    private String sampleLot;
}
