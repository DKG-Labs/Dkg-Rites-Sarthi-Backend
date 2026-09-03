package com.sarthi.Sleeper.entity.FInalCall;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sleeper_batch_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SleeperBatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sleeper_final_result_id")
    @JsonBackReference
    private SleeperFinalResult sleeperFinalResult;

    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "batch_offered_quantity")
    private BigDecimal batchOfferedQuantity;

    @Column(name = "batch_passed_quantity")
    private BigDecimal batchPassedQuantity;

    @Column(name = "batch_rejected_quantity")
    private BigDecimal batchRejectedQuantity;

    @Lob
    @Column(name = "rejected_sleepers", columnDefinition = "LONGTEXT")
    private String rejectedSleepers;

    @Lob
    @Column(name = "epoxy_treated_sleepers", columnDefinition = "LONGTEXT")
    private String epoxyTreatedSleepers;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
