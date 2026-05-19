package com.sarthi.dto.ibsDtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.internal.build.AllowNonPortable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDto {
    private String token;
    private String tokenType;
    private Long expiresIn;
}
