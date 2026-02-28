package com.arth.auth;

import com.arth.auth.dto.RegisterRequest;
import com.arth.auth.service.UserDetailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arth.auth.model.AuthRequest;
import com.arth.auth.model.User;
import com.arth.auth.persist.UserRepository;
import com.arth.auth.utility.JwtUtil;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private  UserRepository userRepository;
    private  JwtUtil jwtUtil;
    private  AuthenticationManager authenticationManager;

    private UserDetailService customUserDetailsService;
    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          AuthenticationManager authenticationManager,
                          UserDetailService customUserDetailsService){
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.customUserDetailsService = customUserDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        customUserDetailsService.registerUser(registerRequest);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<Map> login(@RequestBody AuthRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user  = (User) authentication.getPrincipal();
//        userRepository.findByUsername(request.getUsername())
//                .orElseThrow(() ->new ResponseStatusException(
//                        HttpStatus.UNAUTHORIZED, "Invalid username or password"));
        String token = jwtUtil.generateToken(user);
        return ResponseEntity.ok(Map.of("token",token));
    }
}

