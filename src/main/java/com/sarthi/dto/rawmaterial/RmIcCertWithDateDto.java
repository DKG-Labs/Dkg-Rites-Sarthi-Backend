package com.sarthi.dto.rawmaterial;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO representing an RM IC Certificate with its creation date
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RmIcCertWithDateDto {
    private String certificateNo;
    private LocalDateTime createdAt;
}
