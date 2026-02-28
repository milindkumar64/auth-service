package com.arth.auth.service;

import com.arth.auth.dto.RegisterRequest;
import com.arth.auth.exception.DuplicateUserException;
import com.arth.auth.exception.RoleNotFoundException;
import com.arth.auth.model.Role;
import com.arth.auth.persist.RoleRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.arth.auth.model.User;
import com.arth.auth.persist.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class UserDetailService implements UserDetailsService {

    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    private RoleRepository roleRepository;


    public UserDetailService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    /**
     * @param username
     * @return
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        return org.springframework.security.core.userdetails.User
//                .withUsername(user.getUsername())
//                .password(user.getPassword())
//                .roles(user.getRole())
//                .build();

            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    }

    @Transactional
    public  void registerUser(RegisterRequest registerRequest){
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        Role role = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RoleNotFoundException("RLNF100","Default role not found"));
        user.setRoles(Set.of(role));
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        try {
            userRepository.save(user);
        }
        catch (DataIntegrityViolationException ex){
            throw new DuplicateUserException("DU100","USER ALREADY EXISTS");
        }
    }
}
