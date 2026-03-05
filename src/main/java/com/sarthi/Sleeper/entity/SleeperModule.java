package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "sleeper_module_master")
@Data
public class SleeperModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "module_name")
    private String moduleName;

    @Column(name = "created_date")
    private LocalDateTime createdDate;



}
