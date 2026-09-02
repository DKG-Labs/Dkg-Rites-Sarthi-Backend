package com.sarthi.Sleeper.entity.FInalCall;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "final_call_rejected_sleepers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinalCallRejectedSleeper {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sleeperId;
    private String sleeperCode;
    private String reason;
    private String type;

    @Column(name = "sleeper_final_result_id")
    private Long sleeperFinalResultId;

    @ManyToOne
    @JoinColumn(name = "batch_id")
    private IEBatchSummary batch;
}
