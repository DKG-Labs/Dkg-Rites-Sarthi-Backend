package com.sarthi.Sleeper.entity.FinalInspection;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "inspection_parameter_result")
@Data
public class InspectionParameterResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String parameterResult;

    @ManyToOne
    @JoinColumn(name = "parameter_id")
    private InspectionParameter parameter;

    @ManyToOne
    @JoinColumn(name = "test_result_id")
    private InspectionTestResult testResult;

}
