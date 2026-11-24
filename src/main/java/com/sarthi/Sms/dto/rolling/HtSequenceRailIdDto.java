package com.sarthi.Sms.dto.rolling;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class HtSequenceRailIdDto {
    private String railId;
    private String bloomQuality;
    private Boolean htStatus;
    private List<String> testMarked;
    private String createdAt;
}
