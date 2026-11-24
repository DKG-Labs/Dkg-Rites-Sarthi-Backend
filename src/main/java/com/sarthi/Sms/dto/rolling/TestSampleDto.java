package com.sarthi.Sms.dto.rolling;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TestSampleDto {
    private String heatNumber;
    private Timestamp createdAt;
    private String type;
    private Integer rollingTestSampleId;
}