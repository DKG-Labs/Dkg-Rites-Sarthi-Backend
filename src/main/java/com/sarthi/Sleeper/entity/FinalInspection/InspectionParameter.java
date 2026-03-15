package com.sarthi.Sleeper.entity.FinalInspection;

import jakarta.persistence.*;

@Entity
@Table(name = "inspection_parameter")
public class InspectionParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String parameterName;

    @ManyToOne
    @JoinColumn(name = "module_id")
    private InspectionModule module;
}
