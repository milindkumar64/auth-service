package com.arth.auth;

import com.arth.auth.dto.LoginResponse;
import com.arth.auth.dto.RegisterRequest;
import com.arth.auth.dto.UserDetail;
import com.arth.auth.model.User;
import com.arth.auth.security.AuthService;
import com.arth.auth.service.UserDetailService;
import com.arth.auth.service.UserRegisterService;
import com.arth.auth.utility.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.arth.auth.dto.LoginRequest;

@RestController
@RequestMapping("/auth")
//@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {
    private UserDetailService customUserDetailsService;

    private UserRegisterService userRegisterService;

    private AuthenticationManager authenticationManager;

    private JwtUtil jwtUtil;
    public AuthController(UserDetailService customUserDetailsService,
                          UserRegisterService userRegisterService,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil){
        this.customUserDetailsService = customUserDetailsService;
        this.userRegisterService = userRegisterService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;

    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        userRegisterService.registerUser(registerRequest);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
//        LoginResponse loginResponse =customUserDetailsService.login(request);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        User user = (User) authentication.getPrincipal();
        String token = jwtUtil.generateToken(user);
        return ResponseEntity.ok(new LoginResponse(token, user.getId()));
    }

    @PostMapping("/getUsername/{username}")
    public ResponseEntity<UserDetail> findUserWithRole(@PathVariable String username){
        UserDetail userDetail = customUserDetailsService.findUser(username);
        return ResponseEntity.ok(userDetail);
    }
}

