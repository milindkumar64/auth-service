package com.arth.auth.service;

import com.arth.auth.dto.RegisterRequest;
import com.arth.auth.dto.RoleDto;
import com.arth.auth.dto.UserDetail;
import com.arth.auth.exception.DuplicateUserException;
import com.arth.auth.exception.RoleNotFoundException;
import com.arth.auth.model.Role;
import com.arth.auth.persist.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.arth.auth.model.User;
import com.arth.auth.persist.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDetailService implements UserDetailsService {

    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    private RoleRepository roleRepository;

    private static final Logger log = (Logger) LoggerFactory.getLogger(UserDetailService.class);

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

        log.debug("loadUserByUsername ->IN: loading user .... {}", username);
        UserDetails user = (UserDetails) userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

/*
        List<GrantedAuthority> authorities =
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .collect(Collectors.toList());
        log.info("User {} has roles: {}", username, authorities);
*/

         user =  org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getAuthorities())
                .build();
         return  user;
//            return userRepository.findByUsername(username)
//                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
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
