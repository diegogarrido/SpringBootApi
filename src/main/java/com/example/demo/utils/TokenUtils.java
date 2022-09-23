package com.example.demo.utils;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;

import javax.annotation.PostConstruct;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class TokenUtils {

    public static String secret;
    private static Key hmacKey;

    @Value("${application.JWT.secret}")
    public void setSecret(String prop) {
        secret = prop;
        hmacKey = new SecretKeySpec(Base64.getDecoder().decode(prop), SignatureAlgorithm.HS256.getJcaName());
    }

    public TokenUtils() {
    }

    @PostConstruct
    public void init() {
    }

    public String readToken(String token) {
        Key hmacKey = new SecretKeySpec(Base64.getDecoder().decode(secret), SignatureAlgorithm.HS256.getJcaName());
        try {
            //Verificar token
            token = token.replace("Bearer ", "");
            Jws<Claims> jwt = Jwts.parserBuilder().setSigningKey(hmacKey).build().parseClaimsJws(token);
            return jwt.getBody().get("user_id").toString();
        } catch (Exception e) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "invalid_token");
        }
    }

    public String signToken(User user) {
        return Jwts.builder()
                .claim("user_id", user.getId())
                .setSubject("example_company")
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(3, ChronoUnit.DAYS)))
                .setHeaderParam("typ", "JWT")
                .signWith(hmacKey)
                .compact();
    }
}
