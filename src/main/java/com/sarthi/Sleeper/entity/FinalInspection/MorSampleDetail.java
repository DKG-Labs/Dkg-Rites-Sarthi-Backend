package com.sarthi.Sleeper.entity.FinalInspection;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "mor_sample_detail")
@Data
public class MorSampleDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mor_sample_declaration_id")
    private MorSampleDeclaration declaration;

    @Column(name = "bench_number")
    private String benchNumber;

    @Column(name = "sleeper_no")
    private String sleeperNo;
}
