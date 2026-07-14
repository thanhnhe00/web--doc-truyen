package com.storyreading.storyreadingbackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // Chuỗi bí mật dùng để "ký" token - phải đủ dài (>=32 ký tự) cho thuật toán HS256.
    // TẠM thời hardcode để chạy được; sẽ chuyển vào application.properties ở bước sau.
    private final SecretKey secretKey =
            Keys.hmacShaKeyFor("story-reading-platform-secret-key-2026-cmc-university".getBytes());

    private final long expirationMs = 1000 * 60 * 60 * 24; // 24 giờ

    // Sinh token chứa username và role
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    // Đọc username từ token
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    // Đọc role từ token
    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // Kiểm tra token còn hạn không, có hợp lệ (chữ ký đúng) không
    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}