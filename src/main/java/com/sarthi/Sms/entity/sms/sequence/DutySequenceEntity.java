package com.sarthi.Sms.entity.sms.sequence;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "duty_sequence")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DutySequenceEntity {

    @EmbeddedId
    private DutySequenceId dutySequenceId;

    @Column(name="sequence")
    private int sequence;
    

}
