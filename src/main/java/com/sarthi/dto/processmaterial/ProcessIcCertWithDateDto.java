package com.sarthi.dto.processmaterial;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO representing a Process IC Certificate with its creation date
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessIcCertWithDateDto {
    private String certificateNo;
    private LocalDateTime createdAt;
}
