package com.sarthi.udmcomplaint.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A SARTHI user who can be put in the JI team. type = CM (of the complaint's RIO) or IE. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JiTeamOptionDto {
    private Integer userId;
    private String name;
    private String employeeCode;
    private String region;
    private String type;
}
