package com.sarthi.Sleeper.entity.FinalInspection;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SleeperDetail {
    @Column(name = "sleeper_no")
    private String sleeperNo;

    @Column(name = "sleeper_id")
    private Long sleeperId;
}
