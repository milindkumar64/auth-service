package com.arth.auth.service;

import com.arth.auth.dto.LoginRequest;
import com.arth.auth.dto.LoginResponse;
import com.arth.auth.dto.RegisterRequest;
import com.arth.auth.dto.SignUpRequest;
import com.arth.auth.exception.DuplicateUserException;
import com.arth.auth.exception.RoleNotFoundException;
import com.arth.auth.model.AuthProviderType.AuthProviderType;
import com.arth.auth.model.Role;
import com.arth.auth.model.User;
import com.arth.auth.persist.RoleRepository;
import com.arth.auth.persist.UserRepository;
import com.arth.auth.utility.JwtUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class UserRegisterService {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private  final UserRepository userRepository;
    private  final JwtUtil jwtUtil;


    public UserRegisterService(RoleRepository roleRepository, PasswordEncoder passwordEncoder, UserRepository userRepository, JwtUtil jwtUtil) {
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }


    @Transactional
    public SignUpRequest registerUser(RegisterRequest registerRequest){
        User user = signUpInternal(registerRequest, AuthProviderType.EMAIL,null);
        return new SignUpRequest(user.getId(),user.getUsername());
    }
    public User signUpInternal(RegisterRequest registerRequest, AuthProviderType authProviderType, String providerId){
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setProviderId(providerId);
        user.setProviderType(authProviderType);
        Role role = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RoleNotFoundException("RLNF100","Default role not found"));
        user.setRoles(Set.of(role));
        if(authProviderType == AuthProviderType.EMAIL){
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        }
        try {
            userRepository.save(user);
        }
        catch (DataIntegrityViolationException ex){
            throw new DuplicateUserException("DU100","USER ALREADY EXISTS");
        }
        return user;
    }

}
