package com.sarthi.Sleeper.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "sgci_insert_reading")
@Data
public class SgciInsertReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "heat_no")
    private String heatNo;

    @Column(name = "pattern_no")
    private String patternNo;

    private Double weight;

    @Column(name = "dimensional_not_ok")
    private Boolean dimensionalNotOk;

    @Column(name = "hammer_not_ok")
    private Boolean hammerNotOk;

    private String result;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_id")
    @JsonIgnore
    private SgciInsertAudit audit;
}
