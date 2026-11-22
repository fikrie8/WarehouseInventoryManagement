package com.example.fikrie.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {

    // Added a volatile cached storage key so the random key stays identical
    // for as long as the application is running, preventing internal reload crashes.
    @Value("${app.jwt.secret-key}")
    private String secretKey;
    private volatile SecretKey runtimeCachedKey = null;
    private static final long EXPIRATION_TIME = 86400000;

    public JwtService() {
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .and()
                .signWith(getKey())
                .compact();
    }

    public String generateRefreshToken(HashMap<String, Object> claims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getKey())
                .compact();
    }

    private SecretKey getKey() {
        if(secretKey != null && !secretKey.trim().equalsIgnoreCase("NONE")) {
            try{
                byte[] keyBytes = Decoders.BASE64.decode(secretKey);
                return Keys.hmacShaKeyFor(keyBytes);
            } catch (IllegalArgumentException e) {
                return Keys.hmacShaKeyFor(secretKey.getBytes());
            }
        }

        // 2. Safe Fallback Mode: Generate a dynamic random key if no environment variable exists
        if (runtimeCachedKey == null) {
            synchronized (this) {
                if (runtimeCachedKey == null) {
                    try {
                        KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
                        runtimeCachedKey = keyGenerator.generateKey();
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException("Failed to initialize backup security engine", e);
                    }
                }
            }
        }
        return runtimeCachedKey;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaim(token);
        return claimResolver.apply(claims);
    }

    public Claims extractAllClaim(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String userName = extractUsername(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
