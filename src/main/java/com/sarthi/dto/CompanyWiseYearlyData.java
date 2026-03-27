package com.sarthi.dto;

import com.sarthi.dto.reports.*;
import lombok.Data;

@Data
public class CompanyWiseYearlyData {

    private String manufacturer;
    private String month;

    private Double Accepted;
    private Double inspected;
    private Double processRejected;
    private Double processRejPercent;

    private Double mpiRejected;

    private ShearingDefectsDto shearingDefects;

    private TurningDefectsDto turningDefects;

    private ForgingDefectsDto forgingDefects;

 //   private DimensionalDefectsDto dimensionalDefects;

  //  private VisualDefectsDto visualDefects;

   // private TestingDefectsDto testingDefects;

    private FinishingDefectsDto finishingDefects;

    private QuenchingAllDefectsDto quenchingDefects;

    private TemperingDefectsDto temperingDefects;



}
