package com.gyan.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gyan.dto.LoginRequestDTO;
import com.gyan.dto.UserRequestDTO;
import com.gyan.dto.UserResponseDTO;
import com.gyan.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService; 
    }
    
    @PostMapping("/login")
    public String login(@RequestBody LoginRequestDTO request) {
        return userService.login(request);
    }

    @PostMapping("/register")
    public UserResponseDTO register(@Valid @RequestBody UserRequestDTO request){
        return userService.createUser(request);
    }
}
