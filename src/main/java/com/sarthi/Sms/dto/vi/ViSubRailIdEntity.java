package com.sarthi.Sms.dto.vi;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "visual_inspection_sub_rail_id")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ViSubRailIdEntity.PrimaryKey.class)
public class ViSubRailIdEntity {

    // @Id
    // @Column(name = "heat_number", nullable = false, length = 50)
    // private String heatNo;

    @Id
    @Column(name = "rail_id", nullable = false, length = 50)
    private String railId;

    @Id
    @Column(name = "sub_rail_id", nullable = false, length = 50)
    private String subRailId;

    @Column(name = "accepted_length", precision = 10, scale = 2)
    private BigDecimal acceptedLength;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrimaryKey implements Serializable {

        // @Column(name = "heat_number", nullable = false, length = 50)
        // private String heatNo;

        @Column(name = "rail_id", nullable = false, length = 50)
        private String railId;

        @Column(name = "sub_rail_id", nullable = false, length = 50)
        private String subRailId;
    }
}