package com.arth.auth.security;

import com.arth.auth.dto.LoginResponse;
import com.arth.auth.dto.RegisterRequest;
import com.arth.auth.dto.SignUpRequest;
import com.arth.auth.exception.DuplicateUserException;
import com.arth.auth.exception.RoleNotFoundException;
import com.arth.auth.model.AuthProviderType.AuthProviderType;
import com.arth.auth.dto.LoginRequest;
import com.arth.auth.model.Role;
import com.arth.auth.model.User;
import com.arth.auth.persist.RoleRepository;
import com.arth.auth.persist.UserRepository;
import com.arth.auth.service.UserDetailService;
import com.arth.auth.service.UserRegisterService;
import com.arth.auth.service.TokenService;
import com.arth.auth.utility.JwtUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
public class AuthService {

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final UserRegisterService userRegisterService;

    public AuthService(JwtUtil jwtUtil,
                       TokenService tokenService,
                       UserRepository userRepository,
                       UserRegisterService userRegisterService) {
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.userRegisterService = userRegisterService;
/*        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;*/
    }

    @Transactional
    public ResponseEntity<LoginResponse> handleOAuth2LoginRequest(OAuth2User oAuth2User, String registrationId) throws BadCredentialsException {
        AuthProviderType providerType = jwtUtil.getProviderTypeFromRegistrationId(registrationId);
        String providerId = jwtUtil.determineProviderIdFromOAuth2User(oAuth2User, registrationId);

        User providerUser = userRepository.findByProviderIdAndProviderType(providerId, providerType).orElse(null);
        String email = oAuth2User.getAttribute("email");
        User emailUser = email != null ? userRepository.findByEmail(email).orElse(null) : null;

        User user;
        if (providerUser != null) {
            // Returning OAuth user — issue token
            user = providerUser;
        } else if (emailUser == null) {
            // First-time OAuth signup
            String newUsername = jwtUtil.determineUsernameFromOAuth2User(oAuth2User, registrationId, providerId);
            String newEmail = jwtUtil.determineEmailFromOAuth2User(oAuth2User, registrationId, providerId);
            user = userRegisterService.signUpInternal(
                    new RegisterRequest(newUsername, null, newEmail), providerType, providerId);
        } else {
            // Email already registered via password or another provider
            throw new BadCredentialsException(
                    "An account with this email already exists. Please log in using your existing method.");
        }

        return ResponseEntity.ok(tokenService.issueTokenPair(user));
    }
}
