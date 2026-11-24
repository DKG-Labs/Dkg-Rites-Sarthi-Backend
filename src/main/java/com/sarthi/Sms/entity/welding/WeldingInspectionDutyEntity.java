package com.sarthi.Sms.entity.welding;

import jakarta.persistence.*;
import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "welding_inspection_duty")
public class WeldingInspectionDutyEntity {

    @Id
    @Column(name = "duty_id", nullable = false, length = 50)
    private String dutyId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "shift", nullable = false, length = 50)
    private String shift;

    @Column(name = "mill", nullable = false, length = 10)
    private String mill;


    @Column(name = "welding_line", nullable = false, length = 50)
    private String weldingLine;

    @Column(name = "rail_grade", nullable = false, length = 50)
    private String railGrade;

    @Column(name = "rail_section", nullable = false, length = 50)
    private String railSection;

    @Column(name = "shift_remarks", length = 100)
    private String shiftRemarks;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "machine_serial_no_head", length = 50)
    private String machineSNoHead;

    @Column(name = "machine_serial_no_foot", length = 50)
    private String machineSNoFoot;

    @Column(name = "probe_details_head", length = 50)
    private String probeDetailsHead;

    @Column(name = "probe_details_foot", length = 50)
    private String probeDetailsFoot;

    @Column(name = "probe_db_head", length = 50)
    private String probeDbHead;

    @Column(name = "probe_db_foot", length = 50)
    private String probeDbFoot;

    @Column(name = "remarks_head", length = 50)
    private String remarksHead;
    
    @Column(name = "remarks_foot", length = 50)
    private String remarksFoot;

    @Column(name="joints_welde_prev_shift_bsp2")
    private Integer jointsWeldedPrevShiftBsp2;

    @Column(name="joints_welde_prev_shift_bsp1")
    private Integer jointsWeldedPrevShiftBsp1;

    @Column(name="joints_welde_prev_shift_bsp4")
    private Integer jointsWeldedPrevShiftBsp4;
}
