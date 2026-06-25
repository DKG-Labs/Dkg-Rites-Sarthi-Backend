package com.sarthi.Sleeper.dto;

import java.time.LocalDate;

public interface ProductionDeclarationProjection {

    String getProductionUnit();

    String getBatchNumber();

    LocalDate getCastingDate();

    Integer getTotalCastedSleepers();
}