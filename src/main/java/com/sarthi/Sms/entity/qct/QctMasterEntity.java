package com.sarthi.Sms.entity.qct;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "QCT_MASTER")
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
public class QctMasterEntity {
    
    @Id
    @Column(name="qct_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer qctId;

    @Column(name = "heat_No", nullable = false)
    private String heatNo;

    @Column(name = "strand_No", nullable = false)
    private Integer strandNo;

    @Column(name = "sample_id", length = 30)
    private String sampleId;

    @Column(name = "mill", length = 20)
    private String mill;

    @Column(name = "rail_grade", length = 20)
    private String railGrade;

    @Column(name = "rail_section", length = 20)
    private String railSection;

    @Column(name = "qct_Type", length = 50, nullable = false)
    private String qctType;

    @Column(name = "duty_Id", length = 50, nullable = false)
    private String dutyId;
    
    @Column(name = "created_At", nullable = false)
    private LocalDateTime createdAt;
}