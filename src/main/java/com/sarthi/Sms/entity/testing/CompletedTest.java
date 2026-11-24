package com.sarthi.Sms.entity.testing;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "completed_tests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompletedTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "duty_id", nullable = false)
    private String dutyId;

    @Column(name = "test_type", nullable = false)
    private String testType;

    @Column(name = "heat_number", nullable = false)
    private String heatNumber;

    @Column(name = "strand_number")
    private Integer strandNumber;

    private String mill;
    private String railGrade;
    private String sampleType;
    private String sampleLot;
    private String sampleId;
    private String sampleIdentification;
    
    @Column(name = "chemical_ladle_status")
    private String chemicalLadleStatus;
    
    @Column(name = "chemcial_product_status")
    private String chemicalProductStatus;
    
    @Column(name = "fwt_status")
    private String fwtStatus;
    
    @Column(name = "macro_status")
    private String macroStatus;
    
    @Column(name = "micro_status")
    private String microStatus;
    
    @Column(name = "micro_structure_pearlitic")
    private Boolean microStructurePearlitic;
    
    @Column(name = "tensile_status")
    private String tensileStatus;
    
    @Column(name = "tensile_yield")
    private BigDecimal tensileYield;
    
    @Column(name = "tensile_uts")
    private BigDecimal tensileUts;
    
    @Column(name = "tensile_ei")
    private BigDecimal tensileEi;
    
    @Column(name = "tensile_foot_status")
    private String tensileFootStatus;
    
    @Column(name = "tensile_foot_yield")
    private BigDecimal tensileFootYield;
    
    @Column(name = "tensile_foot_uts")
    private BigDecimal tensileFootUts;
    
    @Column(name = "tensile_foot_ei")
    private BigDecimal tensileFootEi;
    
    @Column(name = "harness_status")
    private String harnessStatus;
    
    @Column(name = "hardenss_1a")
    private BigDecimal hardness1a;
    
    @Column(name = "hardenss_1b")
    private BigDecimal hardness1b;
    
    @Column(name = "hardenss_1c")
    private BigDecimal hardness1c;
    
    @Column(name = "hardenss_2a")
    private BigDecimal hardness2a;
    
    @Column(name = "hardenss_2b")
    private BigDecimal hardness2b;
    
    @Column(name = "hardenss_3")
    private BigDecimal hardness3;
    
    @Column(name = "hardenss_4a")
    private BigDecimal hardness4a;
    
    @Column(name = "hardenss_4b")
    private BigDecimal hardness4b;
    
    @Column(name = "hardenss_rs")
    private BigDecimal hardnessRs;
    
    @Column(name = "ir_status")
    private String irStatus;
    
    @Column(name = "ir_sulphide")
    private BigDecimal irSulphide;
    
    @Column(name = "ir_aluminate")
    private BigDecimal irAluminate;
    
    @Column(name = "ir_silicate")
    private BigDecimal irSilicate;
    
    @Column(name = "ir_oxide")
    private BigDecimal irOxide;
    
    @Column(name = "decarb_status")
    private String decarbStatus;
    
    @Column(name = "decarb_1")
    private BigDecimal decarb1;
    
    @Column(name = "decarb_2")
    private BigDecimal decarb2;
    
    @Column(name = "decarb_3")
    private BigDecimal decarb3;
    
    @Column(name = "n2_status")
    private String n2Status;
    
    @Column(name = "n2")
    private BigDecimal n2;
    
    @Column(name = "o2_status")
    private String o2Status;
    
    @Column(name = "o2")
    private BigDecimal o2;

    @Column(name = "sp_status")
    private String spStatus;

    @Column(name = "sp_value")
    private BigDecimal spValue;

    @Column(name = "rsh_status")
    private String rshStatus;

    @Column(name = "rsh_value")
    private BigDecimal rshValue;

    @Column(name = "ph_status")
    private String phStatus;

    @Column(name = "ph_value")
    private BigDecimal phValue;

    private String remarks;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}