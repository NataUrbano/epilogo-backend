package com.epilogo.epilogo.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {

    private static final String SECRET = "epilogo_secret";
    private static final Algorithm ALGORTIHM = Algorithm.HMAC256(SECRET);

    public String createJwtToken(String username) {
        return JWT.create()
                .withSubject(username)
                .withIssuer("epilogo")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(15)))
                .sign(ALGORTIHM);
    }

    public boolean verifyJwtToken(String token) {
        try {
            JWT.require(ALGORTIHM)
                    .build()
                    .verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public String getUsernameFromJwtToken(String token) {
        return JWT.require(ALGORTIHM)
                .build()
                .verify(token)
                .getSubject();
    }
}
