package com.sarthi.Sleeper.dto.PlantProfile;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class PlantProfileResponseDto {


        private Long id;

        private String plantNameLocation;
        private String vendorCode;
        private String plantType;
        private Integer numberOfSheds;

        private Integer createdBy;
        private LocalDateTime createdDate;

        private Integer updatedBy;
        private LocalDateTime updatedDate;

}
