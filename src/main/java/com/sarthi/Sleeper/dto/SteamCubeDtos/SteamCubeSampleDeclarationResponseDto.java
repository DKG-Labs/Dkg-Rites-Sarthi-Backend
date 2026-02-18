package com.sarthi.Sleeper.dto.SteamCubeDtos;

import lombok.Data;

import java.util.List;

@Data
public class SteamCubeSampleDeclarationResponseDto {

    private Long id;

    private String shedNo;

    private String lineNo;

    private String castingDate;

    private String lbcTime;

    private String batchNo;

    private String concreteGrade;

    private String chamberNo;

    private List<SampleCubeDto> cubes;

    private List<SampleOtherBenchDto> otherBenches;
}
