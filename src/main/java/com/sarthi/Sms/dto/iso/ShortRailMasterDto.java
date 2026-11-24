package com.sarthi.Sms.dto.iso;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShortRailMasterDto {
        private String dutyId;
        private String railSectionInspected;
        private String railGradeInspected;
    
        // Acceptance columns
        private Integer acceptance26mCLA;
        private Integer acceptance26mCLA_01;
        private Integer acceptance26mCLB;
        private Integer acceptance26mIU;
    
        private Integer acceptance25mCLA;
        private Integer acceptance25mCLA_01;
        private Integer acceptance25mCLB;
        private Integer acceptance25mIU;
    
        private Integer acceptance24mCLA;
        private Integer acceptance24mCLA_01;
        private Integer acceptance24mCLB;
        private Integer acceptance24mIU;
    
        private Integer acceptance13mCLA;
        private Integer acceptance13mCLA_01;
        private Integer acceptance13mCLB;
        private Integer acceptance13mIU;
    
        private Integer acceptance12mCLA;
        private Integer acceptance12mCLA_01;
        private Integer acceptance12mCLB;
        private Integer acceptance12mIU;
    
        private Integer acceptance11mCLA;
        private Integer acceptance11mCLA_01;
        private Integer acceptance11mCLB;
        private Integer acceptance11mIU;
    
        private Integer acceptance10mCLA;
        private Integer acceptance10mCLA_01;
        private Integer acceptance10mCLB;
        private Integer acceptance10mIU;
    
        // Rejection columns
        private Integer rejRefRejection26m;
        private Integer rejRefCutbar26m;
        private Integer rejRefRefinish26m;
        private Integer rejRefRejection13m;
        private Integer rejRefCutbar13m;
        private Integer rejRefRefinish13m;
    
        // UTNP columns
        private Integer utnpRejection26m;
        private Integer utnpCutbar26m;
        private Integer utnpRefinish26m;
        private Integer utnpRejection13m;
        private Integer utnpCutbar13m;
        private Integer utnpRefinish13m;
    
        private String remarks;

        // Calculated aggregated fields for Final Inspection Report
        // Prime Quality aggregated values (26m category = 26m+25m+24m, 13m category = 13m+12m+11m+10m)
        private Integer primeQuality26mCLA;
        private Integer primeQuality26mCLB;
        private Integer primeQuality26mIU;
        private Integer primeQuality13mCLA;
        private Integer primeQuality13mCLB;
        private Integer primeQuality13mIU;

        // Total calculations
        private Integer totalClassified26m;
        private Integer totalClassified13m;
        private Integer totalInspected26m;
        private Integer totalInspected13m;
}
