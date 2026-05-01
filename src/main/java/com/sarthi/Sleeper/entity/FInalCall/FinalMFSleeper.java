package com.sarthi.Sleeper.entity.FInalCall;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ie_mf_sleepers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinalMFSleeper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sleeperId;
    private String sleeperCode;

    @ManyToOne
    @JoinColumn(name = "batch_id")
    private IEBatchSummary batch;
}
