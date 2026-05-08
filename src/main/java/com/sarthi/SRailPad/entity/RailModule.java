package com.sarthi.SRailPad.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "Rail_module")
@Data
public class RailModule {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String moduleName;

        private Long createdBy;

        private LocalDateTime createdDate;

        @ManyToOne
        @JoinColumn(name = "workflow_id")
        private RailWorkflow workflow;

}
