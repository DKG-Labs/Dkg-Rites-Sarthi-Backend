package com.sarthi.Sms.entity.ndt;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ndt_duty_clb_prev_data")
public class NdtDutyClbPrevDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "duty_id", nullable = false, length = 50)
    private String dutyId;

    @Column(name = "calibration_id", nullable = false)
    private Integer calibrationId;
}
