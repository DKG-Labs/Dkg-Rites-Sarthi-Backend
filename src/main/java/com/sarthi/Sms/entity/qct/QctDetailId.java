package com.sarthi.Sms.entity.qct;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Data
@Embeddable
public class QctDetailId implements Serializable {

    @Column(name = "heat_no", nullable = false)
    private int heatNo;

    @Column(name = "strand_no", nullable = false)
    private int strandNo;

    @Column(name = "sample_id", nullable = false)
    private String sampleId;

    @Column(name = "qct_type" , nullable = false)
    private String qctType;
}
