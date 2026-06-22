package com.sarthi.Sms.entity.sms.sequence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DutySequenceId implements Serializable{

    @Column(name="duty_type")
    private String dutyType;

    @Column(name="date")
    private LocalDate date;
}
