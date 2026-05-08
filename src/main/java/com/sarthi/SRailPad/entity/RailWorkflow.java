package com.sarthi.SRailPad.entity;

import com.sarthi.Sleeper.entity.SleeperModule;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "rail_workflow")
@Data
public class RailWorkflow {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String workflowName;

        private Long createdBy;

        private LocalDateTime createdDate;

        @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL)
        private List<RailModule> modules;
    }
