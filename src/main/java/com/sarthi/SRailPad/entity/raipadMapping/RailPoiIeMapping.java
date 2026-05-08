package com.sarthi.SRailPad.entity.raipadMapping;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Table(name = "rail_poi_ie_mapping")
@Data
public class RailPoiIeMapping {

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


    @Column(name = "plant_id")
    private String plantId;
}
