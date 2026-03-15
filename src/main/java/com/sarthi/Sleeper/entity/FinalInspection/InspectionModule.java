package com.sarthi.Sleeper.entity.FinalInspection;

import jakarta.persistence.*;

@Entity
@Table(name = "inspection_module")
public class InspectionModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String moduleName;

}