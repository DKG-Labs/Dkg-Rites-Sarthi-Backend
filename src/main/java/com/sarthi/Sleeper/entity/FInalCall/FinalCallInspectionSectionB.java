package com.sarthi.Sleeper.entity.FInalCall;

import com.sarthi.Sleeper.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "final_call_inspection_section_b")
@Data
public class FinalCallInspectionSectionB extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String callNo;

    private LocalDateTime inspectionCallDate;
    private LocalDate inspectionDesiredDate;

    private String rlyPoSr;
    private String itemDesc;

    private String productType;
    private String typeOfErc;

    private String poSrQtyUnit;
    private String consignee;

    private LocalDateTime origDp;
    private LocalDateTime extDp;
    private LocalDate origDpStart;

    private String stageOfInspection;
    private Integer callQtyMt;

    private String placeOfInspection;

    private String processIcNumbers;

    @Column(length = 1000)
    private String remarks;
}
