package com.example.usuarios.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtServiceImpl implements JwtService {

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtServiceImpl(@Value("${app.jwt.secret}") String secret,
                          @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret debe tener al menos 32 caracteres");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationMinutes = expirationMinutes;
    }

    @Override
    public String generarToken(UUID userId, String email) {
        Date ahora = new Date();
        Date vence = new Date(ahora.getTime() + expirationMinutes * 60_000);
        return Jwts.builder()
                .subject(email)
                .claim("id", userId.toString())
                .issuedAt(ahora)
                .expiration(vence)
                .signWith(key)
                .compact();
    }

    @Override
    public Optional<String> obtenerEmail(String token) {
        try {
            return Optional.ofNullable(
                    Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
