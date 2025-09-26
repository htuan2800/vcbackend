package com.vcbackend.config;

import javax.crypto.SecretKey;
import org.springframework.security.core.Authentication;

import com.vcbackend.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

public class JwtProvider {

    private static SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateToken(Authentication auth, int id) {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiration = Date.from(now.plusSeconds(3600)); // 1h

        return Jwts.builder()
                .issuer("volunteer-campaign")
                .issuedAt(issuedAt)
                .expiration(expiration)
                .claim("email", auth.getName())
                .claim("id", String.valueOf(id))
                .claim("type", "access")
                .claim("role", auth.getAuthorities())
                .signWith(getSigningKey())
                .compact();
    }

    public static String refreshAccessToken(User user) {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiration = Date.from(now.plusSeconds(3600)); // 1h

        return Jwts.builder()
                .issuer("volunteer-campaign")
                .issuedAt(issuedAt)
                .expiration(expiration)
                .claim("email", user.getEmail())
                .claim("id", String.valueOf(user.getId()))
                .claim("type", "access")
                .claim("role", user.getRole())
                .signWith(getSigningKey())
                .compact();
    }

    public static String generateRefreshToken(Authentication auth, int id) {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiration = Date.from(now.plusSeconds(365L * 24 * 3600)); // 1 năm

        return Jwts.builder()
                .issuer("vcbackend")
                .issuedAt(issuedAt)
                .expiration(expiration)
                .claim("email", auth.getName())
                .claim("id", String.valueOf(id))
                .claim("type", "refresh")
                .signWith(getSigningKey())
                .compact();
    }

    public static String getEmailFromJwtToken(String jwt) {
        // Remove Bearer prefix if exists
        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }

        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();

        return String.valueOf(claims.get("email"));
    }

    public static String getIdFromJwtToken(String jwt) {
        // Remove Bearer prefix if exists
        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7); // ✅ Thêm dòng này
        }

        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();

        return String.valueOf(claims.get("id"));
    }

    public static boolean validateToken(String token) { // ✅ Thêm static
        try {
            // Remove Bearer prefix if exists
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            System.out.println("Validating token: " + token);
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("Token không hợp lệ: " + e.getMessage());
            return false;
        }
    }

    public static boolean validateRefreshToken(String token) { // ✅ Thêm static
        try {
            // Remove Bearer prefix if exists
            // if (token.startsWith("Bearer ")) {
            //     token = token.substring(7);
            // }
            System.out.println("Validating refresh token: " + token);
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("refresh Token không hợp lệ: " + e.getMessage());
            return false;
        }
    }
}