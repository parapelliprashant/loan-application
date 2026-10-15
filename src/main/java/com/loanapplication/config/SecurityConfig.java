package com.loanapplication.config;

import com.loanapplication.repo.UserRepository;
import com.loanapplication.security.GoogleOAuthSuccessHandler;
import com.loanapplication.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final UserRepository userRepository;
    private final GoogleOAuthSuccessHandler googleOAuthSuccessHandler;
    public SecurityConfig(UserRepository userRepository, GoogleOAuthSuccessHandler googleOAuthSuccessHandler) {
        this.userRepository = userRepository;
        this.googleOAuthSuccessHandler = googleOAuthSuccessHandler;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPassword())
                        .roles(user.getRole().getRoleName())
                        .disabled(!user.getEnabled())
                        .build())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))


                .authorizeHttpRequests(auth -> auth
                        // Public authentication endpoints
                        .requestMatchers(
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/error"
                        ).permitAll()

                        .requestMatchers(
                                "/api/v1/auth/totp/**"
                        ).permitAll()

                        // Customers can view their own KYC documents
                        .requestMatchers(
                                org.springframework.http.HttpMethod.GET,
                                "/api/v1/kyc/documents"
                        ).hasRole("CUSTOMER")

                        // Customers can submit documents using either endpoint
                        .requestMatchers(
                                org.springframework.http.HttpMethod.POST,
                                "/api/v1/kyc/documents",
                                "/api/v1/kyc/*/documents"
                        ).hasRole("CUSTOMER")

                        // Only officers can verify or reject individual documents
                        .requestMatchers(
                                org.springframework.http.HttpMethod.PUT,
                                "/api/v1/kyc/*/verify",
                                "/api/v1/kyc/*/reject"
                        ).hasRole("OFFICER")

                        // Only officers can view customer KYC details
                        .requestMatchers(
                                org.springframework.http.HttpMethod.GET,
                                "/api/v1/kyc/*"
                        ).hasRole("OFFICER")

                        // Only officers can access the start-KYC endpoint
                        .requestMatchers(
                                org.springframework.http.HttpMethod.POST,
                                "/api/v1/kyc/*"
                        ).hasRole("OFFICER")

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                .authenticationProvider(
                        authenticationProvider(
                                userDetailsService(),
                                passwordEncoder()
                        )
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .oauth2Login(oauth -> oauth
                        .successHandler(googleOAuthSuccessHandler)
                );


        return http.build();
    }
}