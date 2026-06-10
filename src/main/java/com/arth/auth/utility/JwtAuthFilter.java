package com.arth.auth.utility;

import com.arth.auth.service.UserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private  JwtUtil jwtUtil;
    private UserDetailService userDetailsService;
    private static final Logger log = (Logger) LoggerFactory.getLogger(JwtAuthFilter.class);


    public JwtAuthFilter(JwtUtil jwtUtil, UserDetailService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try{
                if (!jwtUtil.validateToken(token)) {
                    throw new BadCredentialsException("Invalid JWT token");
                }
                    String username = jwtUtil.extractUsername(token);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    log.debug("loaded user role : {}", userDetails.getAuthorities().toString());

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(auth);
            }
            catch (Exception ex){
                SecurityContextHolder.clearContext();
                throw new BadCredentialsException("Invalid JWT token", ex);
            }
        }

        filterChain.doFilter(request, response);
    }
}

