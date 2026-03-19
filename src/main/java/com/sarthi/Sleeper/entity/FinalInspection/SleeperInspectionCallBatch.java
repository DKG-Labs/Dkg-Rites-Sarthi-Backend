package com.sarthi.Sleeper.entity.FinalInspection;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "sleeper_inspection_call_batch")
@Data
public class SleeperInspectionCallBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_call_id", nullable = false)
    private SleeperInspectionCall inspectionCall;

    @Column(name = "batch_no", nullable = false)
    private String batchNo;

    @ElementCollection
    @CollectionTable(name = "sleeper_ic_good_sleepers", joinColumns = @JoinColumn(name = "batch_id"))
    @Column(name = "sleeper_no")
    private List<String> goodSleepers;

    @ElementCollection
    @CollectionTable(name = "sleeper_ic_bad_sleepers", joinColumns = @JoinColumn(name = "batch_id"))
    @Column(name = "sleeper_no")
    private List<String> badSleepers;
}
