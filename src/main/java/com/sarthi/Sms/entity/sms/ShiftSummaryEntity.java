package com.sarthi.Sms.entity.sms;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "sms_shift_summary_master")
public class ShiftSummaryEntity {

    @Id
    @Column(name = "duty_id")
    private String dutyId;

    @Column(name = "is_ems_functioning")
    private Boolean isEmsFunctioning = false; 

    @Column(name = "is_slag_detector_functioning")
    private Boolean isSlagDetectorFunctioning = false;

    @Column(name = "is_amlc_functioning")
    private Boolean isAmlcFunctioning = false;

    @Column(name = "is_hydrogen_measurement_automatic")
    private Boolean isHydrogenMeasurementAutomatic = false;

    @Column(name = "is_ladle_to_tundish_used")
    private Boolean isLadleToTundishUsed = false;

    @Column(name = "is_tundish_to_mould_used")
    private Boolean isTundishToMouldUsed = false;

    @Column(name = "make_of_casting_powder")
    private String makeOfCastingPowder;

    @Column(name = "make_of_hydris_probe")
    private String makeOfHydrisProbe;
}
