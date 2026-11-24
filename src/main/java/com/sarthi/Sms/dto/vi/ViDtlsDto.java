package com.sarthi.Sms.dto.vi;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class ViDtlsDto {
    private String dutyId;
    private String railId;
    private String heatNo;
    private String date;
    private String shift;
    private String serialNo;
    private String heatStatus;
    private BigDecimal actualOfferedLength;
    private String utFeedback;
    private String dimFeedback;
    private String visualFeedback;
    private Boolean gaugingEndStraightness;
    private Integer rej13;
    private Integer rej12;
    private Integer rej11;
    private Integer rej10;
    private BigDecimal rejCompLength;
    private String remarks;
    private Boolean updateData;
    // private MultipartFile image;
    List<ViAcceptDtlsDto> acptDataList;
    List<ViDefectDtlsDto> defectDataList;
}
