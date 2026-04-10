package com.sarthi.dto.crisDtos;

import lombok.Data;

import java.util.List;

@Data
public class PoRequestDto {

    private PoHeaderDto poHdr;
    private List<PoItemDto> poDtl;

}
