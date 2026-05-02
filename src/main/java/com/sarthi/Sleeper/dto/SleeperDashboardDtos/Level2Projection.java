package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

import java.time.LocalDateTime;

public interface Level2Projection {

    String getPoNo();
    String getSrNo();
    String getConsignee();

    Integer getQty();
    String getUom();

    LocalDateTime getDeliveryDate();
    LocalDateTime getExtendedDeliveryDate();

    Integer getTotalAccepted();     // from calls
    Integer getProcessRejected();   // demoulding
    Integer getFinalRejected();     // inspection
}
