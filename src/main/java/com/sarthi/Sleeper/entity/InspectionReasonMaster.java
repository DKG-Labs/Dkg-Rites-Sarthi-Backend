package com.sarthi.Sleeper.entity;

import com.sarthi.Sleeper.entity.FinalInspection.InspectionParameter;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "inspection_reason_master")
@Data
public class InspectionReasonMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reason_name")
    private String reasonName;

    @ManyToOne
    @JoinColumn(name = "parameter_id")
    private InspectionParameter parameter;

    @ManyToOne
    @JoinColumn(name = "parent_reason_id")
    private InspectionReasonMaster parentReason;

    @OneToMany(mappedBy = "parentReason")
    private List<InspectionReasonMaster> subReasons;

    private Boolean active = true;
}