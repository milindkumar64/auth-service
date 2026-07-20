package com.arth.auth.config;

import com.arth.auth.utility.JwtAuthFilter;
import com.arth.auth.security.OAuth2SuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    private static final Logger log = (Logger) LoggerFactory.getLogger(SecurityConfig.class);


    public SecurityConfig(JwtAuthFilter jwtAuthFilter, OAuth2SuccessHandler oAuth2SuccessHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.cors(cors -> cors.configurationSource(configurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, ex) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"errorMessage\":\"Unauthorized Access1\"}");
                        }).accessDeniedHandler((request, response, ex) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"errorMessage\":\"Access Denied\"}");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/register", "/auth/login", "/auth/refresh", "/auth/logout","/error").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oAuth2 -> oAuth2.failureHandler((request, response, exception) -> {
                    log.error("OAuth2 login failed: {}", exception.getMessage());
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    try {
                        response.getWriter().write("{\"errorMessage\":\"OAuth2 Login Failed\"}");
                    } catch (Exception e) {
                        log.error("Error writing OAuth2 failure response: {}", e.getMessage());
                    }
                                 })
                                .successHandler(oAuth2SuccessHandler)
                );

        return http.build();
    }
    @Bean
    public CorsConfigurationSource configurationSource(){
     CorsConfiguration configuration = new CorsConfiguration();
     configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        // Your frontend URL
     configuration.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE"));
     configuration.setAllowedHeaders(List.of("Authorization","Content-Type"));
     configuration.setAllowCredentials(true);

     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
     source.registerCorsConfiguration("/**",configuration);
     return source;
    }
}

