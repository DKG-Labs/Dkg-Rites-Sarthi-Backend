package com.sarthi.Sms.entity.visualInspection;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViId implements Serializable {
    @Column(name = "heat_number", nullable = false)
    private String heatNo;
    
    @Column(name = "rail_id", nullable = false)
    private String railId;

}
