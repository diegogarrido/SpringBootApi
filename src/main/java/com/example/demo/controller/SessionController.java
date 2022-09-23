package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.Encrypt;
import com.example.demo.utils.TokenUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Controller
@RequestMapping("/auth")
public class SessionController {

    @Autowired
    private UserRepository userRepository;

    private TokenUtils tokenUtils = new TokenUtils();

    @PostMapping("")
    public @ResponseBody String login(@RequestBody User user) {
        //Verificar usuario existe
        Optional<User> exists = userRepository.findByEmail(user.getEmail());
        if (!exists.isPresent()) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "user_not_found");
        }

        //Verificar clave
        if (!Encrypt.verifyUserPassword(user.getPassword(), exists.get().getPassword(), exists.get().getSalt())) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "invalid_credentials");
        }

        return tokenUtils.signToken(exists.get());
    }

    @GetMapping("")
    public @ResponseBody String loginToken(@RequestHeader("Authorization") String token) {
        //Session
        String user_id = tokenUtils.readToken(token);
        Optional<User> sessionUser = userRepository.findById(user_id);
        if (!sessionUser.isPresent()) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "user_not_found");
        }

        return tokenUtils.signToken(sessionUser.get());
    }

}
