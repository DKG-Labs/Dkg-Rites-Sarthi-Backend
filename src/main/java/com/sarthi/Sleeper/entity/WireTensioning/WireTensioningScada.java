package com.sarthi.Sleeper.entity.WireTensioning;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;

@Entity
@Table(name = "wire_tensioning_scada")
@Data
public class WireTensioningScada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plc_time")
    private LocalTime plcTime;

    @Column(name = "bench_no")
    private String benchNo;

    @Column(name = "wire_length")
    private Double wireLength;

    @Column(name = "cross_section")
    private Double crossSection;

    @Column(name = "youngs_modulus")
    private Double youngsModulus;

    @Column(name = "measured_elongation")
    private Double measuredElongation;

    @Column(name = "force_elongation")
    private Double forceElongation;

    @Column(name = "total_load")
    private Double totalLoad;

    @Column(name = "final_load")
    private Double finalLoad;

    @Column(name = "source")
    private String source; // SCADA


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wire_tensioning_id")
    private WireTensioning wireTensioning;
}