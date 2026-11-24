package com.sarthi.Sms.entity.qct;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "qct_testing_master_table")
public class QctTestingMasterEntity {

    @EmbeddedId
    private QctTestingMasterId id;

    @Column(name = "strain")
    private Float strain;

    @Column(name = "frequency_hz")
    private Integer frequencyHz;

    @Column(name = "stress")
    private Float stress;

    @Column(name = "cycle_endured")
    private Float cycleEndured;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "number_of_samples_reported_as_kq")
    private Integer numberOfSamplesReportedAsKq;

    @Column(name = "mean_of_values_kq", nullable = false)
    private Integer meanOfValuesKq;
}
