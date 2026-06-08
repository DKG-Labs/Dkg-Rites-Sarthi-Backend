package com.sarthi.service;

import com.sarthi.entity.UserMaster;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    String generateToken(UserMaster user);
    String extractUserName(String token);
    public String extractUserId(String token);
    boolean isValid(String token, UserDetails user);

    public void validateToken(String authHeader);

}
