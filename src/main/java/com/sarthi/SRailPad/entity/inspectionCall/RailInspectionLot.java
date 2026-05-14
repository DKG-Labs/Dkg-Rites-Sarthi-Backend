package com.sarthi.SRailPad.entity.inspectionCall;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import jakarta.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "rail_inspection_lot")
public class RailInspectionLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "call_id")
    @JsonIgnore
    @ToString.Exclude
    private RailInspectionCall inspectionCall;

    @Column(name = "lot_no")
    private String lotNo;

    @Column(name = "lot_size")
    private Integer lotSize;

    @OneToMany(mappedBy = "lot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RailInspectionBatch> batches;
}
