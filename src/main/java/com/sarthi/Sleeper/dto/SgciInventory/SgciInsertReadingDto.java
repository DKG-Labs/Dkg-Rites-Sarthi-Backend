package com.sarthi.Sleeper.dto.SgciInventory;

import lombok.Data;
import java.util.List;

@Data
public class SgciInsertReadingDto {
    private String heatNo;
    private String patternNo;
    private Double weight;
    private Boolean dimensionalNotOk;
    private Boolean hammerNotOk;
    private String result;
}
