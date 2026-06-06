package com.sarthi.SRailPad.entity.ieVerification;

import lombok.Data;
import lombok.ToString;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "rail_final_inspection_section_results")
public class RailFinalInspectionSectionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_result_id", nullable = false)
    @ToString.Exclude
    private RailFinalInspectionLotResults lotResult;

    @Column(name = "section_key", nullable = false)
    private String sectionKey;

    @Column(name = "section_name", nullable = false)
    private String sectionName;

    @Column(name = "sample_size")
    private String sampleSize;

    @Column(name = "status")
    private String status;
}
