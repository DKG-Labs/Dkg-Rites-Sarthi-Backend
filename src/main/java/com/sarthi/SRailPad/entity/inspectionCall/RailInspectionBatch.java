package com.sarthi.SRailPad.entity.inspectionCall;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import jakarta.persistence.*;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "rail_inspection_batch")
public class RailInspectionBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    @JsonIgnore
    @ToString.Exclude
    private RailInspectionLot lot;

    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "production_date")
    private LocalDate productionDate;
}
