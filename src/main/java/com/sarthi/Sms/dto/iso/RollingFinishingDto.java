package com.sarthi.Sms.dto.iso;

import java.util.List;

import com.sarthi.Sms.dto.rolling.HotStampingDto;
import com.sarthi.Sms.dto.rolling.PreCamberingDto;
import com.sarthi.Sms.dto.rolling.StraighteningMachineDto;
import lombok.Data;

@Data
public class RollingFinishingDto {
    private List<HotStampingDto> hotStampingList;
    private List<PreCamberingDto> preCamberingList;
    private List<StraighteningMachineDto> straighteningMachineList;
}
