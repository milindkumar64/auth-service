package com.arth.auth.config;

import com.arth.auth.utility.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
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

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.cors(cors -> cors.configurationSource(configurationSource()))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, ex) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Unauthorized\"}");
                        })
                )
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/register", "/auth/login","/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

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

