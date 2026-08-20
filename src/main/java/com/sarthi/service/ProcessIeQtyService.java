package com.sarthi.service;

import com.sarthi.dto.InspectionQtySummaryResponse;
import com.sarthi.dto.TotalManufaturedQtyOfPoDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProcessIeQtyService {

    public List<InspectionQtySummaryResponse> getQtySummary(String requestId);

    public String getpoNumberByCallNo(String requestedId);

    public TotalManufaturedQtyOfPoDto getTotalManufaturedQtyPo(String heatNo, String poNo);

    public TotalManufaturedQtyOfPoDto getTotalManufaturedQtyPo(String heatNo, String poNo, String callNo);

    public TotalManufaturedQtyOfPoDto getTotalManufaturedQtyPo(String heatNo, String poNo, String callNo, String vendorCode);

    int getAcceptedQtyForLot(String requestId, String lotNumber, String heatNo);

}
