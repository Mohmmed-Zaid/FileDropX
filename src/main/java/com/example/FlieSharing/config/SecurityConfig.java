package com.example.FlieSharing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/dropfilex/", // Permit access to the new login page (your home.html)
                                "/login/**",
                                "/oauth2/**",
                                "/login/oauth2/code/**",  // OAuth2 callback URLs
                                "/dropfilex/s/**", // Shared links
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/favicon.ico",
                                "/logo.jpg",  // Add logo access
                                "/error"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        // Set custom login page to where your home.html is served by FileController
                        .loginPage("/dropfilex/")
                        .defaultSuccessUrl("/dropfilex/files", true)
                        // Update failure URL to redirect back to the correct login page
                        .failureUrl("/dropfilex/?error=true")
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/oauth2/authorization"))
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/login/oauth2/code/*"))
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        // Update logout success URL to redirect back to the correct login page
                        .logoutSuccessUrl("/dropfilex/?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/dropfilex/s/**")
                );

        return http.build();
    }
}
