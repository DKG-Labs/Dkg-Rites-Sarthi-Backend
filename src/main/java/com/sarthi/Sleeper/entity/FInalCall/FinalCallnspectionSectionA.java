package com.sarthi.Sleeper.entity.FInalCall;

import com.sarthi.Sleeper.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "final_call_inspection_section_a")
@Data
public class FinalCallnspectionSectionA extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String callNo;

    private String rlyPoNo;
    private LocalDateTime poDate;

    private Integer poQty;
    private String vendorName;

    private String maNo;
    private LocalDate maDate;

    private String purchasingAuthority;
    private String billPayingOfficer;
}