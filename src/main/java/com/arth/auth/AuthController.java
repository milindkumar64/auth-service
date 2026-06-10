package com.arth.auth;

import com.arth.auth.dto.LoginResponse;
import com.arth.auth.dto.RefreshTokenRequest;
import com.arth.auth.dto.RegisterRequest;
import com.arth.auth.dto.UserDetail;
import com.arth.auth.model.User;
import com.arth.auth.service.TokenService;
import com.arth.auth.service.UserDetailService;
import com.arth.auth.service.UserRegisterService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.arth.auth.dto.LoginRequest;

import java.util.Map;

@RestController
@RequestMapping("/auth")
//@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {
    private UserDetailService customUserDetailsService;

    private UserRegisterService userRegisterService;

    private AuthenticationManager authenticationManager;

    private TokenService tokenService;

    public AuthController(UserDetailService customUserDetailsService,
                          UserRegisterService userRegisterService,
                          AuthenticationManager authenticationManager,
                          TokenService tokenService) {
        this.customUserDetailsService = customUserDetailsService;
        this.userRegisterService = userRegisterService;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
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
        return ResponseEntity.ok(tokenService.issueTokenPair(user));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(tokenService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        tokenService.revoke(request.getRefreshToken());
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/getUsername/{username}")
    public ResponseEntity<UserDetail> findUserWithRole(@PathVariable String username){
        UserDetail userDetail = customUserDetailsService.findUser(username);
        return ResponseEntity.ok(userDetail);
    }
}

