package com.raghav.runboxspringboot.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private static final String ROLE_CLAIM = "role";
    private static final String USER_ID_CLAIM = "userId";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtService(
            @Value("${security.jwt.secret}") String secretKey,
            @Value("${security.jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
            @Value("${security.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generateAccessToken(UUID userId, String username, String role) {
        return buildToken(userId, username, role, ACCESS_TOKEN_TYPE, accessTokenExpirationMs, UUID.randomUUID());
    }

    public String generateRefreshToken(UUID userId, String username, String role) {
        return buildToken(userId, username, role, REFRESH_TOKEN_TYPE, refreshTokenExpirationMs, UUID.randomUUID());
    }

    public String generateRefreshToken(UUID userId, String username, String role, UUID tokenId) {
        return buildToken(userId, username, role, REFRESH_TOKEN_TYPE, refreshTokenExpirationMs, tokenId);
    }

    public Claims extractClaims(String token) {
        return parseClaims(token);
    }

    public Claims validateAccessToken(String token) {
        Claims claims = parseClaims(token);
        if (!ACCESS_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new JwtException("Invalid token type");
        }
        return claims;
    }

    public Claims validateRefreshToken(String token) {
        Claims claims = parseClaims(token);
        if (!REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new JwtException("Invalid token type");
        }
        return claims;
    }

    public UUID extractUserId(Claims claims) {
        String value = claims.get(USER_ID_CLAIM, String.class);
        if (value == null || value.isBlank()) return null;
        return UUID.fromString(value);
    }

    public String extractRole(Claims claims) {
        return claims.get(ROLE_CLAIM, String.class);
    }

    public UUID extractTokenId(Claims claims) {
        String tokenId = claims.getId();
        if (tokenId == null) throw new JwtException("Missing token id");
        return UUID.fromString(tokenId);
    }

    private String buildToken(UUID userId, String username, String role, String tokenType, long expirationMs, UUID tokenId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(USER_ID_CLAIM, userId.toString());
        claims.put(ROLE_CLAIM, role);
        claims.put(TOKEN_TYPE_CLAIM, tokenType);

        Instant now = Instant.now();
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .id(tokenId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .and()
                .signWith(signingKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}