package com.sarthi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "poi_process_ie_mapping",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee_code", "poi_code"}))
@Data
public class PoiProcessIeMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_code", nullable = false)
    private String employeeCode;

    @Column(name = "poi_code", nullable = false)
    private String poiCode;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", updatable = false)
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_date")
    private Date updatedDate;
}
