package com.sarthi.dto.crisDtos;

import lombok.Data;

import java.util.List;

@Data
public class MaRequestDto {

    private MaHeaderDto maHdr;
    private List<MaDetailDto> maDtl;

}
