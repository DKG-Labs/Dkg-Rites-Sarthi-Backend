package com.sarthi.service.Impl;

import java.util.Date;
import java.util.function.Function;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.sarthi.entity.UserMaster;
import com.sarthi.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secretkey}")
    private String secretKey;

    @Value("${jwt.token.validity}")
    private Long validity;

    @Override
    public String generateToken(UserMaster user) {
        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + validity))
                .signWith(getKey())
                .compact();
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }
    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public boolean isValid(String token, UserDetails user) {
        if (user instanceof UserMaster) {
            String tokenUserId = extractUserId(token);
            return tokenUserId.equals(String.valueOf(((UserMaster) user).getUserId())) &&
                    !extractClaim(token, Claims::getExpiration).before(new Date());
        }
        return false;
    }

    public void validateToken(String authHeader) {

        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {

            throw new RuntimeException("Missing token");
        }

        String token = authHeader.substring(7);

        try {

          extractUserName(token);

        } catch (Exception e) {

            throw new RuntimeException("Invalid token");
        }
    }
}
