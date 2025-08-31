package co.edu.uniquindio.retos.service;

import co.edu.uniquindio.retos.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class TokenService {

    private final SecretKey key;
    private final long expirationSeconds;

    public TokenService(@Value("${jwt.secret}") String secret,
                        @Value("${jwt.expiration-seconds:3600}") long expirationSeconds) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date exp = Date.from(now.plusSeconds(expirationSeconds));

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("nombre", user.getNombre())
                .claim("email", user.getEmail())
                .setIssuedAt(issuedAt)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
