package com.epilogo.epilogo.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
@Tag(name = "JWT Service", description = "Servicio para operaciones relacionadas con tokens JWT")
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    @Operation(summary = "Extraer nombre de usuario", description = "Extrae el nombre de usuario del token JWT")
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Operation(summary = "Extraer reclamación", description = "Extrae una reclamación específica del token JWT")
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    @Operation(summary = "Generar token", description = "Genera un token JWT para un usuario")
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    @Operation(summary = "Generar token con reclamaciones", description = "Genera un token JWT con reclamaciones adicionales")
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    @Operation(summary = "Generar token de refresco", description = "Genera un token de refresco JWT")
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    @Hidden
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .setIssuer(jwtIssuer)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Operation(summary = "Validar token", description = "Valida si un token JWT es válido para un usuario específico")
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    @Hidden
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    @Hidden
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @Hidden
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error("JWT token expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            throw e;
        }
    }

    @Hidden
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}