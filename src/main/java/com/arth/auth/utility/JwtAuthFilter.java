package com.arth.auth.utility;

import com.arth.auth.service.TokenBlacklistService;
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
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserDetailService userDetailsService;
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private static final List<String> PUBLIC_URLS = List.of(
            "/auth/login",
            "/auth/register",
            "/auth/logout"
    );
    public JwtAuthFilter(JwtUtil jwtUtil,
                         TokenBlacklistService tokenBlacklistService,
                         UserDetailService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        return PUBLIC_URLS.stream()
                .anyMatch(url -> request.getServletPath().startsWith(url));
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
                String jti = jwtUtil.extractJti(token);
                if (tokenBlacklistService.isBlacklisted(jti)) {
                    throw new BadCredentialsException("JWT token has been revoked");
                }
                Long userId = jwtUtil.extractUserId(token);

                    UserDetails userDetails = userDetailsService.loadUserById(userId);

                    log.debug("Authenticated user id {} with roles {}", userId, userDetails.getAuthorities());

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

