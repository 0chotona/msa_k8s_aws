package com.example.user.provider;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtProvider {
    @Value("${jwt.secret}")
    private String secret;

    private final long ACCESS_TOKEN_EXPIRE = 1000l * 60 * 30;
    private final long REFRESH_TOKEN_EXPIRE = 1000l * 60 * 60 * 24 * 7;

    private Key getStringKey() {
        System.out.println(">>> Provider jwt secret : " + secret);
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // access token provider
    public String createAT(String email) {
        System.out.println(">>> Provider createAT : " + email);
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE))
                .signWith(getStringKey())
                .compact();
    }

    public String createRT(String email) {
        System.out.println(">>> Provider createRT : " + email);
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE))
                .signWith(getStringKey())
                .compact();
    }

    // token 에서 subject추출
    // Bearer xxxxxxxx
    public String getUserEmailFromToken(String token) {
        System.out.println(">>> Provider getUserEmailFromToken token : " + token);
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Claims claims = Jwts.parser()
                .setSigningKey(getStringKey())
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public long getATE() {
        return ACCESS_TOKEN_EXPIRE;
    }

    public long getRTE() {
        return REFRESH_TOKEN_EXPIRE;
    }
}
