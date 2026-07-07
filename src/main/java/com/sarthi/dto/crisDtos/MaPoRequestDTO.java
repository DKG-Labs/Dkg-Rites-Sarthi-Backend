package com.sarthi.dto.crisDtos;

import lombok.Data;

import java.util.List;

@Data
public class MaPoRequestDTO {

    private String status;

    private String message;

    private List<String> error;

    private String timestamp;

    private MaPoDataDTO data;

}
