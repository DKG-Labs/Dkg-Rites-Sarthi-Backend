package com.sarthi.Sms.entity.qct;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "qct_detail")
public class QctDetailEntity {

    @EmbeddedId
    private QctDetailId id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "duty_id", nullable = false)
    private String dutyId;
}
