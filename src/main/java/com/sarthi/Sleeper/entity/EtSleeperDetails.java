package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "et_sleeper_details")
@Data
public class EtSleeperDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sleeperId;     // actual sleeper id
    private String sleeperNo;

    @ManyToOne
    @JoinColumn(name = "et_id")
    private EpoxyTreatedSleeper et;

}
