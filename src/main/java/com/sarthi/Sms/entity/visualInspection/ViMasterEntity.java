package com.sarthi.Sms.entity.visualInspection;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="visual_inspection_master")
public class ViMasterEntity {

    // @EmbeddedId
    // private ViId viId;

    @Column(name = "heat_number", nullable = false)
    private String heatNo;
    
    @Id
    @Column(name = "rail_id", nullable = false)
    private String railId;

    @Column(name = "duty_id", nullable = false)
    private String dutyId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "shift", nullable = false, length = 10)
    private String shift;

    @Column(name = "serial_number", nullable = false, length = 10)
    private String serialNo;

    @Column(name = "heat_status", nullable = false, length = 50)
    private String heatStatus;

    @Column(name = "actual_offered_length", nullable = false)
    private BigDecimal actualOfferedLength;

    @Column(name = "ut_feedback", length = 100)
    private String utFeedback;

    @Column(name = "dim_feedback", length = 100)
    private String dimFeedback;

    @Column(name = "visual_feedback", length = 100)
    private String visualFeedback;

    @Column(name = "gauging_end_straightness")
    private Boolean gaugingEndStraightness = false;

    @Column(name = "rej_13m")
    private Integer rej13;

    @Column(name = "rej_12m")
    private Integer rej12;

    @Column(name = "rej_11m")
    private Integer rej11;

    @Column(name = "rej_10m")
    private Integer rej10;

    @Column(name = "rej_comp_length")
    private BigDecimal rejCompLength;

    @Column(name = "remarks", length = 50)
    private String remarks;

    @Lob
    @Column(name = "image")
    private byte[] image;
}