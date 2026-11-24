package com.sarthi.Sms.entity.rolling;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="ht_sequence_heat")
public class HtSequenceHeatEntity {

    @Id
    @Column(name = "heat_number")
    private String heatNo;

    @Column(name = "batch_create_duty_id")
    private String batchCreateDutyId;

    @Column(name = "batch_end_duty_id")
    private String batchEndDutyId;

    @Column(name = "ht_sequence_batch_complete")
    private Boolean htSequenceBatchComplete;
}
