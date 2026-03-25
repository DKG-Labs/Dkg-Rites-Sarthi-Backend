package com.sarthi.Sleeper.dto;

import lombok.Data;

@Data
public class BenchGroupResponseDTO {

    private Integer benchOrGangNumber;

    private String sleeperType;     // sleeperCode
    private Integer noOfMoulds;

    private Integer count;
    private Double rft;

}
