package com.sarthi.Sleeper.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "sleeper_poi_ie_mapping")
@Data
public class SleeperPoiIeMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "poi_code", nullable = false, length = 50)
    private String poiCode;

    @Column(name = "ie_user_id", nullable = false)
    private Integer ieUserId;

    @Column(name = "ie_type", length = 20)
    private String ieType;   // MAIN_IE or PROCESS_IE

    @Column(name = "created_date")
    private LocalDateTime createdDate = LocalDateTime.now();
}