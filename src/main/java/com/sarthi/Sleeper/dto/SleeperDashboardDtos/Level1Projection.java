package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

import java.sql.Timestamp;

public interface Level1Projection {

    String getRly();
    String getPoNo();
    Timestamp getPoDate();
    String getVendor();
    String getRegion();

    Integer getPoQty();
    Integer getAccQty();
    Integer getTotalRejected();
    Integer getTotalOffered();
}