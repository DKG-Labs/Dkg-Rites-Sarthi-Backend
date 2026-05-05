package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

public interface MprProjection {

    String getRly();
    String getPoNo();
    String getManufacturer();

    Integer getPoQty();

    Integer getDispatchedInPeriod();
    Integer getTotalDispatched();
}