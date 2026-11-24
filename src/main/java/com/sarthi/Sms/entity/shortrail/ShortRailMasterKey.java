package com.sarthi.Sms.entity.shortrail;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class ShortRailMasterKey implements Serializable {


    @Column(name = "Rail_Section_Inspected", nullable = false)
    private String railSectionInspected;

    @Column(name = "Rail_Grade_Inspected", nullable = false)
    private String railGradeInspected;

    @Column(name = "duty_id", nullable = false)
    private String dutyId;
}

