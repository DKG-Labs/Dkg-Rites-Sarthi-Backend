package com.sarthi.service;

import com.sarthi.dto.vendorDtos.VendorPoHeaderResponseDto;
import org.hibernate.sql.exec.spi.StandardEntityInstanceResolver;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface VendorPoService {

   // public List<VendorPoHeaderResponseDto> getPoListByVendorCode(String vendorCode, String vendorType);
   public List<VendorPoHeaderResponseDto> getPoListByVendorCode(String vendorCode);


}
