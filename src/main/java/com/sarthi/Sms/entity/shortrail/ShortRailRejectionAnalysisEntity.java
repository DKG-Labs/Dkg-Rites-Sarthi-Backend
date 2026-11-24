package com.sarthi.Sms.entity.shortrail;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Data
@Table(name="short_rail_rejection_analysis")
public class ShortRailRejectionAnalysisEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "duty_id", length = 50, nullable = false)
    private String dutyId;

    @Column(name = "Rail_Section_Inspected", length = 50, nullable = false)
    private String railSectionInspected;

    @Column(name = "Rail_Grade_Inspected", length = 50, nullable = false)
    private String railGradeInspected;

    @Column(name = "defect_26m", length = 30)
    private String defect26m;

    @Column(name = "defect_26m_count")
    private Integer defect26mCount;

    @Column(name = "defect_13m", length = 30)
    private String defect13m;

    @Column(name = "defect_13m_count")
    private Integer defect13mCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
