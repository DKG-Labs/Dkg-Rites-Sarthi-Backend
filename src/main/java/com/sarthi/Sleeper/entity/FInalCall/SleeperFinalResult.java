package com.sarthi.Sleeper.entity.FInalCall;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sleeper_final_result")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SleeperFinalResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_number", nullable = false)
    private String callNumber;

    @Column(name = "po_no")
    private String poNo;

    @Column(name = "sr_no")
    private String srNo;

    @Column(name = "shift")
    private String shift;

    @Column(name = "date_of_inspection")
    private LocalDate dateOfInspection;

    @Column(name = "sleeper_type")
    private String sleeperType;

    @Column(name = "total_offered_quantity")
    private BigDecimal totalOfferedQuantity;

    @Column(name = "total_accepted")
    private BigDecimal totalAccepted;

    @Column(name = "total_rejected")
    private BigDecimal totalRejected;

    @Column(name = "plant_id")
    private String plantId;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "sleeperFinalResult", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<SleeperBatchResult> batchResults = new ArrayList<>();
}
