package com.gyan.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserRequestDTO {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid Email Format")
    private String email;

    @NotBlank(message =  "Password cannot be empty")
    private String password;
    
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

}