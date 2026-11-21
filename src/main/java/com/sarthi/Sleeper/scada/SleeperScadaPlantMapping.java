package com.sarthi.Sleeper.scada;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Sleeper plant -> SCADA gateway codes used in topics SPLR/&lt;vendor&gt;/&lt;plant&gt;/&lt;line&gt;/&lt;STAGE&gt;. */
@Entity
@Table(name = "sleeper_scada_plant_mapping")
@Getter
@Setter
public class SleeperScadaPlantMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_short_name", nullable = false, length = 20)
    private String vendorShortName;

    @Column(name = "vendor_name", length = 200)
    private String vendorName;

    @Column(name = "plant_short_name", nullable = false, length = 20)
    private String plantShortName;

    @Column(name = "plant_name", length = 200)
    private String plantName;

    @Column(name = "plant_id", length = 100)
    private String plantId;

    /** Comma list, e.g. "U1,U2". */
    @Column(name = "line_ids", nullable = false, length = 100)
    private String lineIds;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
