package com.sarthi.Sleeper.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sleeper_workflow")
@Data
public class SleeperWorkflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String workflowName;

    private Long createdBy;

    private LocalDateTime createdDate;

    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL)
    private List<SleeperModule> modules;
}