package com.gyan.service;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.gyan.dto.LoginRequestDTO;
import com.gyan.dto.UserRequestDTO;
import com.gyan.dto.UserResponseDTO;
import com.gyan.entity.User;
import com.gyan.model.Role;
import com.gyan.repository.UserRepository;
import com.gyan.security.JwtUtil;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDTO createUser(UserRequestDTO request) { 
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
            request.getEmail(),
            hashedPassword,
            Role.USER
        );
        
        User savedUser = userRepository.save(user);
        return new UserResponseDTO(
            savedUser.getId(),
            savedUser.getEmail()
        );
    }

    public String login(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow();

        boolean valid = passwordEncoder.matches(
            request.getPassword(),
            user.getPassword()
        );

        if(!valid) {
            throw new RuntimeException("Invalid credentials");
        }

        return JwtUtil.generateToken(user.getEmail(), user.getRole());
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
        .stream()
        .map(user -> new UserResponseDTO(
            user.getId(),
            user.getEmail()
        )).toList();
    }

    public UserResponseDTO getUser(Long id){
        User user = userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserResponseDTO(
            user.getId(),
            user.getEmail()
        );
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
