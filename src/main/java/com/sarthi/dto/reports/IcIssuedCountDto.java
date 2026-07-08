package com.sarthi.dto.reports;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IcIssuedCountDto {
    private long total;
    private long rmCount;
    private long processCount;
    private long finalCount;
}
