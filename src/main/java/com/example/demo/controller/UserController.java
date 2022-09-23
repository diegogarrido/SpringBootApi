package com.example.demo.controller;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.Encrypt;
import com.example.demo.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Date;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private TokenUtils tokenUtils = new TokenUtils();

    @PostMapping("")
    public @ResponseBody User createUser(@RequestBody User user) {
        //Validaciones
        Optional<User> exists = userRepository.findByEmail(user.getEmail());
        if (exists.isPresent()) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "email_exists");
        }
        if (user.getName() == null || user.getName().length() == 0 || user.getName().length() > 20) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "invalid_name");
        }
        if (user.getPassword() == null || user.getPassword().length() == 0 || user.getPassword().length() < 6) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "invalid_password");
        }

        //Encriptar clave
        user.setSalt(Encrypt.getSaltValue(30));
        user.setPassword(Encrypt.generateSecurePassword(user.getPassword(), user.getSalt()));

        //Por defecto usuario normal convertir a admin desde bd

        //Guardar en bd
        userRepository.save(user);
        return user;
    }

    @PutMapping("")
    public @ResponseBody User updateUser(@RequestHeader("Authorization") String token, @RequestBody User user) {
        //Session
        String user_id = tokenUtils.readToken(token);
        Optional<User> sessionUser = userRepository.findById(user_id);
        if (!sessionUser.isPresent()) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "user_not_found");
        }
        //Usuario existe
        Optional<User> exists = userRepository.findById(user.getId());
        if (!exists.isPresent()) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "user_not_found");
        }
        //Validaciones
        if (!user.getEmail().equals(exists.get().getEmail()) && userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "email_exists");
        }
        if (user.getName() == null || user.getName().length() == 0 || user.getName().length() > 20) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "invalid_name");
        }
        if (user.getPassword() == null || user.getPassword().length() == 0 || user.getPassword().length() < 6) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "invalid_password");
        }

        //No puede editar su rol si no es admin
        if(user.getRole() != exists.get().getRole() && sessionUser.get().getRole() != Role.SUPER_ADMIN){
            throw new HttpServerErrorException(HttpStatus.FORBIDDEN);
        }

        //Debe ser el mismo usuario o admin
        if (!exists.get().getId().equals(sessionUser.get().getId()) && sessionUser.get().getRole() != Role.ADMIN) {
            throw new HttpServerErrorException(HttpStatus.FORBIDDEN);
        }

        //Encriptar clave si es nueva
        if (!user.getPassword().equals(exists.get().getPassword())) {
            user.setSalt(Encrypt.getSaltValue(30));
            user.setPassword(Encrypt.generateSecurePassword(user.getPassword(), user.getSalt()));
        }

        user.setCreatedAt(exists.get().getCreatedAt());
        user.setUpdatedAt(new Date());

        //Guardar en bd
        userRepository.save(user);
        return user;
    }

}
