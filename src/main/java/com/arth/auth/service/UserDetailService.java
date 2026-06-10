package com.arth.auth.service;

import com.arth.auth.dto.*;
import com.arth.auth.utility.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.arth.auth.model.User;
import com.arth.auth.persist.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailService implements UserDetailsService {

    private UserRepository userRepository;



    private static final Logger log = (Logger) LoggerFactory.getLogger(UserDetailService.class);

    public UserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * @param username
     * @return
     */
    @Override
    public UserDetails loadUserByUsername(String username) {

        log.debug("loadUserByUsername ->IN: loading user .... {}", username);
        UserDetails user = (UserDetails) userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<GrantedAuthority> authorities =
                user.getAuthorities().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                        .collect(Collectors.toList());
        log.info("User {} has roles: {}", username, authorities);

//         user =  org.springframework.security.core.userdetails.User   // REMOVED: this builder strips the id field,
//                .withUsername(user.getUsername())                       // causing ClassCastException in AuthController.
//                .password(user.getPassword())                           // User entity already implements UserDetails — return it directly.
//                .authorities(user.getAuthorities())
//                .build();
//         return  user;
             return userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

/*
    @Transactional
    public  SignUpRequest registerUser(RegisterRequest registerRequest){
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
        SignUpRequest signUpRequest = new SignUpRequest(user.getId(),user.getUsername());
        return signUpRequest;
    }
*/
/*
public LoginResponse login (LoginRequest request){
    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
    );
    User user = (User) authentication.getPrincipal();
    String token = jwtUtil.generateToken(user);
    return new LoginResponse(token, user.getId());
}
*/


    public UserDetail findUser(String username) {
       User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("USER DOES NOT EXISTS"));
       UserDetail userDetail = new UserDetail();
       userDetail.setUsername(user.getUsername());
       userDetail.setEmail(user.getEmail());
       RoleDto roleDto = new RoleDto(user.getRoles());
       userDetail.setRoles(roleDto.getRole());
       return userDetail;
    }
}
