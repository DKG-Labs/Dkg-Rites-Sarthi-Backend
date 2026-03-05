package com.sarthi.dto.reports;

import com.sarthi.dto.QuenchingDefectsDto;
import com.sarthi.dto.TemperingDefectsDto;
import lombok.Data;

@Data
public class FourthLevelInspectionDto {


        private BasicDetailsDto basicDetails;

        private ProcessQtyDto processQty;

        private ShearingDefectsDto shearingDefects;

        private TurningDefectsDto turningDefects;

        private ForgingDefectsDto forgingDefects;

        private DimensionalDefectsDto dimensionalDefects;

        private VisualDefectsDto visualDefects;

        private TestingDefectsDto testingDefects;

        private FinishingDefectsDto finishingDefects;

        private QuenchingDefectsDto quenchingDefects;

        private TemperingDefectsDto temperingDefects;



}


