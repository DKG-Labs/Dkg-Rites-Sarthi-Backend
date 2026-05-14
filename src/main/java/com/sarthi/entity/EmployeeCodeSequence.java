package com.sarthi.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "employee_code_sequence")
@Data
public class EmployeeCodeSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roleCode;

    private String zoneCode;

    private Integer lastNumber;
}
